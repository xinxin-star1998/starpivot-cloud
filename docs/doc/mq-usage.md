# StarPivot MQ 使用说明

本文档说明 `starpivot-mq` Starter 的架构、配置、开发接入方式，以及在 RabbitMQ 管理台中的验证方法。

## 1. 概述

| 项 | 说明 |
|----|------|
| 中间件 | RabbitMQ 3.x（带 Management 插件） |
| 封装模块 | `starpivot-mq`（Starter jar，**不单独部署**） |
| 消息契约 | `starpivot-api/event`（Exchange / Queue / RoutingKey 常量） |
| 默认状态 | **关闭**（`starpivot.mq.enabled=false`），不影响现有 Feign 同步调用 |

### 设计原则

- **Feign**：同步鉴权、查询等强一致场景继续使用。
- **MQ**：审计日志、跨服务异步任务等允许最终一致的场景使用。
- **消费端必须在业务服务内**：例如登录日志由 `starpivot-system` 消费落库，不能集中到独立 MQ 微服务。

### 模块依赖关系

```
starpivot-auth / starpivot-system
        │
        ▼
  starpivot-mq（Starter）
        │
        ├── starpivot-api（event 常量 + DTO）
        └── starpivot-common（TraceId 等）
```

---

## 2. 快速启用

### 2.1 启动 RabbitMQ

```bash
docker compose up -d rabbitmq
```

| 端口 | 用途 |
|------|------|
| 5672 | AMQP 协议 |
| 15672 | Web 管理台 |

管理台地址：http://localhost:15672  
默认账号：`admin` / `admin`

### 2.2 发布 Nacos 配置

将 `nacos/config/mq-config.yaml` 发布到 Nacos：

```powershell
.\nacos\import-config.ps1
```

`starpivot-auth` 与 `starpivot-system` 的 `application.yml` 已 optional import 该配置：

```yaml
- optional:nacos:mq-config.yaml?group=${NACOS_GROUP:DEFAULT_GROUP}&refreshEnabled=true
```

### 2.3 启动服务

```bash
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth
```

### 2.4 验证登录日志链路

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

确认方式：

1. RabbitMQ 管理台 → **Queues** → `starpivot.system.login-log` 有 Incoming / Ack
2. 数据库 `sys_logininfor` 有新记录
3. auth 日志无 `回退 Feign` 警告

---

## 3. 配置说明

### 3.1 公共配置（`common-config.yaml`）

```yaml
starpivot:
  mq:
    enabled: ${MQ_ENABLED:false}
```

默认关闭；未启用时 `StarPivotMqEnvironmentPostProcessor` 会自动排除 `RabbitAutoConfiguration`，**无需 RabbitMQ 即可启动**。

### 3.2 MQ 专用配置（`mq-config.yaml`）

```yaml
starpivot:
  mq:
    enabled: ${MQ_ENABLED:true}

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:127.0.0.1}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin}
    virtual-host: ${RABBITMQ_VHOST:/}
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
        retry:
          enabled: true
          initial-interval: 1000ms
          max-attempts: 3
          multiplier: 2.0
```

### 3.3 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MQ_ENABLED` | `false` | 是否启用 MQ（common 与 mq-config 一致；启用审批/登录日志等场景时设为 `true` 并 re-import） |
| `RABBITMQ_HOST` | `127.0.0.1` | Broker 地址 |
| `RABBITMQ_PORT` | `5672` | AMQP 端口 |
| `RABBITMQ_USERNAME` | `admin` | 用户名 |
| `RABBITMQ_PASSWORD` | `admin` | 密码 |
| `RABBITMQ_VHOST` | `/` | 虚拟主机 |

---

## 4. 拓扑结构

### 4.1 Exchange

| 名称 | 类型 | 用途 |
|------|------|------|
| `starpivot.topic` | topic | 领域事件广播 |
| `starpivot.direct` | direct | 点对点任务 |
| `starpivot.dlx` | topic | 死信交换机 |

### 4.2 Queue 与 Routing Key

| Routing Key | Queue | 生产者 | 消费者 | 状态 |
|-------------|-------|--------|--------|------|
| `audit.login-log.created` | `starpivot.system.login-log` | auth | system | **已接入** |
| `audit.oper-log.created` | `starpivot.system.oper-log` | 各服务 | system | 预留 |
| `job.oper-log.clean` | `starpivot.system.job-handler` | monitor | system | 预留 |
| `approval.instance.finished.mall.purchase` | `starpivot.mall.approval-finished.purchase` | approval | ware | **已接入** |
| `approval.instance.finished.mall.spu` | `starpivot.mall.approval-finished.spu` | approval | product | **已接入** |
| `approval.instance.finished.mall.return` | `starpivot.mall.approval-finished.return` | approval | order | **已接入** |
| `approval.instance.finished.mall.coupon` | `starpivot.mall.approval-finished.coupon` | approval | promotion | **已接入** |

拓扑在 `starpivot-mq` 的 `RabbitTopologyConfiguration` 中声明，**system 启动且 MQ 启用时自动创建**。

### 4.3 拓扑示意

```
                    starpivot.topic
                          │
         audit.login-log.created
                          │
                          ▼
              starpivot.system.login-log ──► LoginLogMqListener ──► sys_logininfor
                          │
                    (失败重试耗尽)
                          ▼
                    starpivot.dlx
```

---

## 5. 消息格式

所有消息使用统一信封 `MessageEnvelope`：

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "traceId": "abc123",
  "eventType": "audit.login-log.created",
  "occurredAt": "2026-06-22T17:30:00",
  "producer": "starpivot-auth",
  "version": "1",
  "payload": {
    "userName": "admin",
    "ipaddr": "127.0.0.1",
    "loginLocation": "内网IP",
    "browser": "Chrome",
    "os": "Windows",
    "status": "0",
    "msg": "登录成功"
  }
}
```

### HTTP Header 对应关系

| Header | 说明 |
|--------|------|
| `X-Trace-Id` | 链路追踪 ID（与网关 / Feign 一致） |
| `X-Message-Id` | 幂等键 |
| `X-Event-Type` | 事件类型 |

---

## 6. 开发接入指南

### 6.1 Maven 依赖

在需要发送或消费消息的业务模块 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>cn.org.starpivot</groupId>
    <artifactId>starpivot-mq</artifactId>
</dependency>
```

### 6.2 发送消息（生产者）

**推荐唯一入口**：注入 `MqPublisher`，不要直接使用 `RabbitTemplate`。

```java
@Service
@RequiredArgsConstructor
public class YourProducerService {

    private final ObjectProvider<MqPublisher> mqPublisherProvider;

    public void sendEvent(YourDto dto) {
        MqPublisher publisher = mqPublisherProvider.getIfAvailable();
        if (publisher == null) {
            // MQ 未启用，走 Feign 或其他降级逻辑
            return;
        }
        publisher.publish(
                MqRoutingKeys.YOUR_ROUTING_KEY,
                MqRoutingKeys.YOUR_ROUTING_KEY,
                dto);
    }
}
```

参考实现：`starpivot-auth` 的 `LoginLogRecordService`（MQ 优先，失败回退 Feign）。

### 6.3 消费消息（消费者）

继承 `AbstractMqListener`，使用手动 ACK 与幂等校验：

```java
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class YourMqListener extends AbstractMqListener<YourDto> {

    public YourMqListener(MqMessageConverter messageConverter,
                          ObjectProvider<IdempotentChecker> idempotentCheckerProvider) {
        super(messageConverter, idempotentCheckerProvider);
    }

    @RabbitListener(
            queues = MqQueueNames.YOUR_QUEUE,
            ackMode = "MANUAL",
            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)
    public void onMessage(Message message, Channel channel) throws IOException {
        handleMessage(message, channel, YourDto.class, this::handleBusiness);
    }

    private void handleBusiness(YourDto dto) {
        // 业务处理
    }
}
```

参考实现：`starpivot-system` 的 `LoginLogMqListener`。

### 6.4 新增一种消息的步骤

1. 在 `starpivot-api/event/MqRoutingKeys` 添加 Routing Key 常量
2. 在 `starpivot-api/event/MqQueueNames` 添加 Queue 常量（如需新队列）
3. 在 `RabbitTopologyConfiguration` 声明 Queue 与 Binding
4. 生产者服务：调用 `MqPublisher.publish(...)`
5. 消费者服务：新增 `*MqListener` 类

### 6.5 契约常量位置

| 类 | 路径 |
|----|------|
| `MqExchangeNames` | `starpivot-api/.../event/MqExchangeNames.java` |
| `MqRoutingKeys` | `starpivot-api/.../event/MqRoutingKeys.java` |
| `MqQueueNames` | `starpivot-api/.../event/MqQueueNames.java` |

---

## 7. Starter 核心组件

| 类 | 职责 |
|----|------|
| `StarPivotMqAutoConfiguration` | 自动配置入口，需 `starpivot.mq.enabled=true` |
| `StarPivotMqEnvironmentPostProcessor` | MQ 关闭时排除 Rabbit 自动配置 |
| `MqPublisher` | 统一发送（信封封装、TraceId、Confirm） |
| `MqMessageConverter` | 消息反序列化 |
| `AbstractMqListener` | 消费模板（幂等 + 手动 ACK） |
| `RedisIdempotentChecker` | Redis SETNX 幂等（需 Redis） |
| `RabbitTopologyConfiguration` | Exchange / Queue / Binding 声明 |

自动加载文件：

```
starpivot-mq/src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
  org.springframework.boot.env.EnvironmentPostProcessor
```

---

## 8. RabbitMQ 管理台查看

### 8.1 入口

http://localhost:15672 → 登录 `admin` / `admin`

### 8.2 查看 StarPivot 资源

| 页签 | 查看内容 |
|------|----------|
| **Queues and Streams** | `starpivot.system.login-log` 等队列；Consumers 应为 1（system 消费端） |
| **Exchanges** | `starpivot.topic`、`starpivot.direct`、`starpivot.dlx` |
| **Connections** | auth、system 各一条连接 |

### 8.3 查看消息内容

1. 进入 **Queues** → 点击 `starpivot.system.login-log`
2. 展开 **Get messages** → Ack mode 选 `Nack message requeue true` → **Get Message(s)**
3. 可看到 JSON 格式的 `MessageEnvelope`

### 8.4 查看消息速率

**Overview** 页：

- **Publish**：auth 发送时短暂升高
- **Deliver / Ack**：system 消费时升高

---

## 9. 故障排查

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 看不到 `starpivot.*` 队列 | system 未启动或 MQ 未启用 | 确认 `mq-config.yaml` 已发布且 `MQ_ENABLED=true` |
| auth 日志出现「回退 Feign」 | RabbitMQ 未启动或连接失败 | `docker compose up -d rabbitmq`，检查 host/port |
| 队列有消息但不消费 | system 未启动或 Listener 未加载 | 确认 `starpivot.mq.enabled=true`，查看 system 日志 |
| 重复落库 | 幂等失效 | 确认 Redis 可用；检查 `messageId` |
| 服务启动报 Rabbit 连接错误 | MQ 关闭但配置冲突 | 确认 `common-config` 中 `enabled=false` 且未加载 mq-config |
| 消息进入 DLX | 消费异常且重试耗尽 | **Queues** 筛选 DLX 绑定队列，查看 Payload 与 system 错误日志 |

### 健康检查

```bash
curl http://localhost:8082/actuator/health
```

启用 MQ 后响应中应包含 `rabbit` 组件状态。

### 日志关键字

| 服务 | 关键字 |
|------|--------|
| auth | `MQ 消息已发送`、`MQ 发送登录日志失败，回退 Feign` |
| system | `MQ 登录日志已落库`、`MQ 消息消费失败`、`跳过重复 MQ 消息` |

---

## 10. 已接入场景：登录日志

### 流程

```
Client → Gateway → auth.login()
                      │
                      ├─ MQ 启用 → MqPublisher → starpivot.topic
                      │                              │
                      │                              ▼
                      │                    starpivot.system.login-log
                      │                              │
                      │                              ▼
                      │                    LoginLogMqListener → sys_logininfor
                      │
                      └─ MQ 未启用 / 发送失败 → Feign → system /internal/logininfor
```

### 相关代码

| 角色 | 类 |
|------|-----|
| 发送 | `cn.org.starpivot.auth.service.LoginLogRecordService` |
| 消费 | `cn.org.starpivot.system.mq.LoginLogMqListener` |
| DTO | `cn.org.starpivot.api.system.dto.LoginLogDto` |

---

## 11. 后续扩展（预留）

| 场景 | Routing Key | 说明 |
|------|-------------|------|
| 操作日志 | `audit.oper-log.created` | 跨服务 `@Log` 统一落库 |
| 清操作日志 | `job.oper-log.clean` | monitor Quartz 触发，替代 Feign |
| 缓存失效 | `cache.config.changed` | 配置变更广播（待设计） |

扩展时遵循本文档第 6.4 节步骤即可。
