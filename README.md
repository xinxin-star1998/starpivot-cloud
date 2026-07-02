# StarPivot Cloud

星枢微服务 — 基于 **Spring Cloud Alibaba** 的企业级微服务脚手架，涵盖认证授权、系统管理、文件中心、代码生成、监控调度、商城（B 端 + C 端 Portal + 微信小程序）与审批中台，配套 **Vue 3** 管理前端。

---

## 项目概览


| 维度     | 说明                                                        |
| ------ | --------------------------------------------------------- |
| 架构     | 前后端分离 · 网关统一入口 · Nacos 注册/配置 · Feign 服务间调用                |
| API 前缀 | `/api/v1`（可通过 `API_VERSION` 调整）                           |
| 默认账号   | `admin` / `admin123`（来自 `sql/star_pivot.sql`，若已修改请使用实际密码） |
| 开发模式   | 混合模式（Docker 中间件 + 本地微服务）或全 Docker 部署                      |


### 架构示意

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────────────────────────┐
│ star-pivot  │────▶│   Gateway    │────▶│ auth / system / file / mall / ...   │
│    -ui      │     │   :8080      │     │         (Nacos 服务发现)             │
│   :3000     │     └──────────────┘     └─────────────────────────────────────┘
└─────────────┘              │                              │
                             JWT 校验                    Feign /internal/**
                             TraceId 透传                X-Internal-Token
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
                 MySQL           Redis          RabbitMQ
                :3307            :6379           :5672
                    │
                 Nacos :8848 · Zipkin :9411
```

---



## 技术栈


| 组件                   | 版本         |
| -------------------- | ---------- |
| Java                 | 17         |
| Spring Boot          | 3.3.13     |
| Spring Cloud         | 2023.0.1   |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos                | 2.3.x      |
| MyBatis-Plus         | 3.5.12     |
| Vue                  | 3.5.x      |
| Element Plus         | 2.11.x     |
| Vite                 | 6.x        |


---



## 模块结构

```
starpivot-cloud/
├── starpivot-cloud-dependencies/   # BOM — 统一依赖版本
├── starpivot-common/                 # 公共组件：JWT、Security、Redis、Tracing
├── starpivot-api/                    # Feign 接口与跨服务 DTO
├── starpivot-mq/                     # RabbitMQ 发布/消费模板
├── starpivot-gateway/                # API 网关
├── starpivot-auth/                   # 认证授权
├── starpivot-system/                 # 系统管理（用户/角色/菜单/部门/字典…）
├── starpivot-file/                   # 文件上传与文件中心
├── starpivot-generator/              # 代码生成
├── starpivot-monitor/                # 监控与 Quartz 定时任务
├── starpivot-mall/                   # 商城 B 端 + C 端 Portal
├── starpivot-approval/               # 审批中台
├── star-pivot-ui/                    # Vue 3 管理前端 + H5 Portal
├── star-pivot-mp/                    # 微信小程序（uni-app）
├── nacos/config/                     # Nacos 配置模板
├── sql/                              # 数据库初始化脚本
├── docker/                           # 容器构建文件
└── docs/                             # 设计文档与部署指南
```



### 服务端口


| 服务                  | 端口   | 说明           |
| ------------------- | ---- | ------------ |
| starpivot-gateway   | 8080 | 统一 API 入口    |
| starpivot-auth      | 8081 | 登录、Token、验证码 |
| starpivot-system    | 8082 | RBAC、系统配置    |
| starpivot-generator | 9203 | 代码生成         |
| starpivot-monitor   | 9204 | 在线用户、定时任务    |
| starpivot-mall      | 9205 | 商城 B/C 端     |
| starpivot-approval  | 9206 | 审批流程         |
| starpivot-file      | 9202 | 文件服务         |
| star-pivot-ui       | 3000 | 前端开发服务器      |


---



## 鉴权模型

```
登录:  Client → Gateway → Auth → Feign → System(/internal/user) → MySQL
业务:  Client → Gateway(JWT 校验) → 各微服务(MicroserviceAuthenticationFilter)
内部:  Feign → /internal/** + X-Internal-Token（不经网关）
```

权限策略（`roles-only` / `menu-permission` / `custom`）详见 [docs/security/permission-strategy.md](docs/security/permission-strategy.md)。

---



## 快速开始



### 前置要求

- JDK 17+
- Maven 3.8+
- Docker Desktop（推荐 ≥ 8GB 内存）
- Node.js ≥ 20.19、pnpm ≥ 8.8（前端）



### 1. 启动基础设施

```bash
docker compose up -d
```

将启动以下组件：


| 组件       | 地址                                                         | 默认凭据          |
| -------- | ---------------------------------------------------------- | ------------- |
| Nacos    | [http://localhost:8848/nacos](http://localhost:8848/nacos) | nacos / nacos |
| MySQL    | 127.0.0.1:**3307**                                         | root / root   |
| Redis    | 127.0.0.1:6379                                             | 密码 root       |
| RabbitMQ | [http://localhost:15672](http://localhost:15672)           | admin / admin |
| Zipkin   | [http://localhost:9411](http://localhost:9411)             | —             |


MySQL 容器**首次启动**会自动执行：

- `sql/00_create_mall_database.sql` — 创建 `star_pivot_mall` 库
- `sql/star_pivot.sql` — 核心库表与初始数据（含 RBAC、审批、代码生成等）
- `sql/sys_menu.sql` — 补充菜单

**商城业务数据**（约 7.8MB，未随容器自动导入）需手动执行：

```bash
mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_mall < sql/star_pivot_mall.sql
```

> 数据库名：`star_pivot`（系统库）、`star_pivot_mall`（商城库）。Nacos 中 `DB_URL` 默认已指向 `star_pivot`。



### 2. 导入 Nacos 配置

```powershell
# PowerShell — 至少设置 JWT 密钥（≥ 32 字符）
$env:JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
.\nacos\import-config.ps1
```

```bash
# Linux / macOS
export JWT_SECRET="dev-local-jwt-secret-must-be-at-least-32-chars"
export INTERNAL_SERVICE_TOKEN="dev-internal-token"
chmod +x nacos/import-config.sh
./nacos/import-config.sh
```

配置说明见 [nacos/README.md](nacos/README.md)。

### 3. 编译项目

```bash
mvn clean install -DskipTests
```



### 4. 启动微服务

**最小集（登录 + 系统管理）：**

```bash
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth
mvn spring-boot:run -pl starpivot-gateway
```

**按需启动其他模块（各开终端）：**

```bash
mvn spring-boot:run -pl starpivot-file
mvn spring-boot:run -pl starpivot-generator
mvn spring-boot:run -pl starpivot-monitor
mvn spring-boot:run -pl starpivot-mall
mvn spring-boot:run -pl starpivot-approval
```



### 5. 启动前端

```bash
cd star-pivot-ui
pnpm install
pnpm dev
```

前端默认 [http://localhost:3000，Vite](http://localhost:3000，Vite) 代理将 `/api/*` 转发至网关 [http://localhost:8080。](http://localhost:8080。)

### 6. 验证

对外 API 统一前缀 `/api/v1`。登录须携带图形验证码。


| 步骤    | 方法   | 路径                            |
| ----- | ---- | ----------------------------- |
| 获取验证码 | GET  | `/api/v1/auth/captcha?scene=login` |
| 登录    | POST | `/api/v1/auth/login`          |
| 用户信息  | GET  | `/api/v1/auth/user/info`      |
| 健康检查  | GET  | `/api/v1/health`              |


登录请求体示例：

```json
{
  "username": "admin",
  "password": "admin123",
  "captchaToken": "<GET /captcha 返回>",
  "captcha": "abcd"
}
```

说明：验证码与账号密码在同一次登录请求中校验；密码错误时验证码可继续使用，无需重新获取。

### API 路径命名约定

列表与删除接口统一带业务名，前后端保持一致：

| 操作 | 路径格式 | 示例（用户模块 `@RequestMapping("/sys/user")`） |
|------|----------|--------------------------------------------------|
| 分页列表 | `POST /{entity}PageList` | `POST /sys/user/userPageList` |
| 删除 | `DELETE /remove{Entity}` | `DELETE /sys/user/removeUser` |

嵌套资源示例：审批待办 `POST /approval/task/todoTaskPageList`；采购明细删除 `DELETE /mall/purchase/removePurchaseDetail`。

### 接口文档（Knife4j）

各微服务使用 **Knife4j** 提供 OpenAPI 文档（不含 Swagger 默认 UI）。启动后访问：

- 直连服务：`http://localhost:{port}/api/v1/doc.html`（如 auth `8081`、system `8082`、mall `9205`）
- 经网关：对应服务路由下的 `/doc.html`（网关白名单已放行）

代码中继续使用 `@Tag`、`@Operation` 等 OpenAPI 3 注解（`io.swagger.v3.oas.annotations`）。

登录后在请求头携带 `Authorization: Bearer <token>`。响应头含 `X-Trace-Id` 便于链路排查。

---

## 审批与 MQ 联调

StarPivot 中 MQ 默认**关闭**（`MQ_ENABLED=false`）。审批完结回调、登录日志异步落库等场景需显式启用 RabbitMQ。

### 联调架构

**采购审批（主链路）**

```
用户提交采购单
  → mall 更新 audit_status=PENDING
  → Feign 调 approval /internal/approval/instance/submit
  → 审批人待办 approve / reject（多步阶梯）
  → approval 事务提交后发 MQ（approval.instance.finished）
  → mall 消费 starpivot.mall.approval-finished
  → 回写 wms_purchase.audit_status = APPROVED / REJECTED / WITHDRAWN
```

**登录日志（轻量验证 MQ 是否通）**

```
auth 登录成功 → MqPublisher → starpivot.system.login-log → system 落库 sys_logininfor
（MQ 不可用时自动回退 Feign）
```

### 前置条件

| 项 | 要求 |
|----|------|
| 基础设施 | `docker compose up -d` 含 RabbitMQ（5672 / 管理台 15672） |
| 数据库 | 已导入 `star_pivot_mall.sql`（采购单等业务表） |
| 微服务 | 至少启动 `gateway`、`auth`、`system`、`mall`、`approval` |
| 配置 | `INTERNAL_SERVICE_TOKEN` 各服务一致；Redis 可用（消费幂等） |
| 默认模板 | `sql/star_pivot.sql` 已含 `mall_purchase_default`（部门负责人 → 财务） |

### 启用 MQ

```powershell
# 1. 设置环境变量后重新导入 Nacos（覆盖 common-config 中的 MQ_ENABLED）
$env:JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
$env:MQ_ENABLED = "true"
.\nacos\import-config.ps1

# 2. 重启相关微服务（auth / system / mall / approval 均 optional import mq-config.yaml）
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth
mvn spring-boot:run -pl starpivot-mall
mvn spring-boot:run -pl starpivot-approval
mvn spring-boot:run -pl starpivot-gateway
```

启用后 RabbitMQ 管理台应出现 Exchange `starpivot.topic`，以及队列：

| 队列 | Routing Key | 生产者 | 消费者 |
|------|-------------|--------|--------|
| `starpivot.system.login-log` | `audit.login-log.created` | auth | system |
| `starpivot.mall.approval-finished` | `approval.instance.finished` | approval | mall |

管理台：http://localhost:15672（admin / admin）

### 场景 A：登录日志 MQ（推荐先跑通）

完成验证码 + 登录后检查：

1. **Queues** → `starpivot.system.login-log` 有 Deliver / Ack
2. 数据库 `star_pivot.sys_logininfor` 新增记录
3. auth 日志**无**「回退 Feign」警告

详见 [docs/doc/mq-usage.md](docs/doc/mq-usage.md) §2.4。

### 场景 B：采购审批端到端

#### 方式一：前端操作（推荐）

完整分步骤说明见 **[docs/doc/approval-test-guide.md](docs/doc/approval-test-guide.md)**（含采购/退货/优惠券/SPU、驳回、撤回、通知、超时、统计看板等场景）。

1. 登录管理端（默认 `admin`，具备财务角色，可连续审批两关）
2. **商城 → 仓储 → 采购单**：选择 `audit_status=DRAFT` 的采购单（如 id=1），点击「提交审批」
3. **审批中心 → 待办**：依次处理「部门负责人」「财务审批」
4. 回到采购单详情，确认 `audit_status` 变为 `APPROVED`

#### 方式二：API 逐步调用

```bash
# 0. 登录并取得 Token（须先完成验证码流程，略）

# 1. 提交采购单审批（DRAFT 状态方可提交）
curl -X POST http://localhost:8080/api/v1/mall/purchase/1/submit-approval \
  -H "Authorization: Bearer <token>"

# 2. 查询待办，取得 taskId
curl -X POST http://localhost:8080/api/v1/approval/task/todoTaskPageList \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'

# 3. 审批通过（每一关各调用一次，替换 taskId）
curl -X POST http://localhost:8080/api/v1/approval/task/approve \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"taskId": 1, "comment": "同意"}'
```

最后一关通过后，MQ 投递 `approval.instance.finished`，mall 消费并更新业务表。

#### 验收检查

```sql
-- 采购单审批状态应为 APPROVED
SELECT id, audit_status, approval_instance_id
FROM star_pivot_mall.wms_purchase
WHERE id = 1;

-- 审批实例应为 APPROVED
SELECT instance_id, status, finish_time
FROM star_pivot.ap_instance
ORDER BY instance_id DESC
LIMIT 1;
```

RabbitMQ **Queues** → `starpivot.mall.approval-finished`：最后一关通过后应有 Incoming / Ack。

mall 日志关键字：`采购单审批完结`；approval / mq 日志：`MQ 消息已发送`（routingKey=`approval.instance.finished`）。

### 故障排查

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 看不到 `starpivot.*` 队列 | MQ 未启用或 mall/system 未启动 | 确认 `MQ_ENABLED=true` 且已 re-import Nacos |
| 审批通过但 `audit_status` 仍为 PENDING | mall 未启 MQ 或消费失败 | 查 mall 日志、Redis 幂等、队列是否堆积 |
| auth 出现「回退 Feign」 | RabbitMQ 未启动 | `docker compose up -d rabbitmq` |
| 提交审批报内部 Token 错误 | 服务间 Token 不一致 | 统一 `INTERNAL_SERVICE_TOKEN` 并重启 |
| 待办列表为空 | 当前用户非审批人 | 检查 `ap_task.assignee_id` 与模板步骤（DEPT_LEADER / ROLE finance） |
| 消息进入 DLX | 消费异常且重试耗尽 | 查 system/mall 错误日志与 DLX 队列 Payload |

更多 MQ 接入与扩展说明见 [docs/doc/mq-usage.md](docs/doc/mq-usage.md)；审批 API 与表结构见 [docs/doc/workflow-design.md](docs/doc/workflow-design.md)。

---

## Docker 全量部署

除混合开发模式外，支持基础设施 + 全部微服务 + 前端容器化一键启动：

```powershell
# Windows
.\scripts\docker-up.ps1 -Mode full

# Linux / macOS
./scripts/docker-up.sh full
```

分层 compose、访问地址与启动顺序详见 [docs/docker-deploy.md](docs/docker-deploy.md)。

---



## 环境变量

完整说明见 [nacos/README.md](nacos/README.md)。常用变量：


| 变量                                | 默认值                                      | 说明                            |
| --------------------------------- | ---------------------------------------- | ----------------------------- |
| `NACOS_SERVER`                    | 127.0.0.1:8848                           | Nacos 地址                      |
| `API_VERSION`                     | v1                                       | API 版本段，影响 context-path 与网关路由 |
| `DB_URL`                          | jdbc:mysql://127.0.0.1:3307/star_pivot?… | 数据库连接                         |
| `DB_USERNAME`                     | root                                     | 数据库用户                         |
| `DB_PASSWORD`                     | root                                     | 数据库密码                         |
| `REDIS_HOST`                      | 127.0.0.1                                | Redis 主机                      |
| `REDIS_PASSWORD`                  | root                                     | Redis 密码                      |
| `JWT_SECRET`                      | （见 common-config）                        | JWT 签名密钥，至少 32 字符             |
| `INTERNAL_SERVICE_TOKEN`          | （空）                                      | 服务间 `/internal/**` 调用 Token   |
| `INTERNAL_SERVICE_TOKEN_REQUIRED` | false                                    | 为 true 时未配置 Token 将拒绝内部接口     |
| `MQ_ENABLED`                      | false                                    | 是否启用 RabbitMQ（审批回调、登录日志等）     |
| `TRACING_ENABLED`                 | true                                     | 是否启用 Zipkin 分布式追踪             |


---



## 新增业务模块

1. 在根目录创建 `starpivot-xxx` 模块，继承父 POM
2. 在 `starpivot-cloud-dependencies/pom.xml` 中声明内部模块版本
3. 在 `starpivot-gateway` 的 `application.yml` 中添加路由规则
4. 在根 `pom.xml` 的 `<modules>` 中注册新模块
5. 在 `nacos/config/` 添加 `starpivot-xxx.yaml` 并执行 import 脚本
6. 按 [权限策略文档](docs/security/permission-strategy.md) 选择 `authority-strategy`

---



## 可观测性

详见 [docs/observability.md](docs/observability.md)。

- **TraceId**：网关与各 Servlet 微服务自动生成/透传 `X-Trace-Id`，日志格式含 `%X{traceId}`
- **Actuator**：各服务暴露 `health`、`info`、`metrics`、`prometheus`
- **Zipkin**：`docker compose up -d zipkin` 后访问 [http://localhost:9411](http://localhost:9411)
- **Feign**：内部调用自动透传 TraceId 与 `X-Internal-Token`

---



## 相关文档


| 文档                                                                           | 说明             |
| ---------------------------------------------------------------------------- | -------------- |
| [nacos/README.md](nacos/README.md)                                           | 配置中心结构与导入      |
| [docs/docker-deploy.md](docs/docker-deploy.md)                               | Docker 混合/全量部署 |
| [docs/security/permission-strategy.md](docs/security/permission-strategy.md) | 权限策略           |
| [docs/observability.md](docs/observability.md)                               | 链路追踪与指标        |
| [docs/mall.md](docs/mall.md)                                                 | 商城菜单与接口对照      |
| [docs/doc/mq-usage.md](docs/doc/mq-usage.md)                                 | MQ 使用说明        |
| [docs/doc/workflow-design.md](docs/doc/workflow-design.md)                   | 审批开发规格         |
| [Java微服务版本审批中心设计方案.md](Java微服务版本审批中心设计方案.md)                                 | 审批中心设计方案       |


---



## License

本项目仅供学习与内部开发参考使用。