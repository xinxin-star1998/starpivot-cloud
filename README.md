# starpivot-cloud

星枢微服务项目 — Spring Cloud Alibaba 微服务骨架。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.3.x |
| Spring Cloud | 2023.0.x |
| Spring Cloud Alibaba | 2023.0.x |
| Nacos | 2.3.x |
| MyBatis-Plus | 3.5.x |

## 模块结构

```
starpivot-cloud/
├── starpivot-cloud-dependencies/   # BOM 依赖版本管理
├── starpivot-common/               # 公共组件、JWT、Security、Redis
├── starpivot-api/                  # Feign 接口与跨服务 DTO
├── starpivot-mq/                   # RabbitMQ 发布/消费模板
├── starpivot-gateway/              # API 网关 (8080)
├── starpivot-auth/                 # 认证授权服务 (8081)
├── starpivot-system/               # 系统管理服务 (8082)
├── starpivot-file/                 # 文件服务 (9202)
├── starpivot-generator/            # 代码生成 (9203)
├── starpivot-monitor/              # 监控与定时任务 (9204)
├── starpivot-mall/                 # 商城 B 端 + C 端 Portal (9205)
├── starpivot-approval/             # 审批中台
└── star-pivot-ui/                  # Vue 前端 (3000)
```

## 鉴权流程

```
登录: Client → Gateway → Auth → Feign → System(/internal/user) → MySQL
业务: Client → Gateway(JWT 校验) → 各微服务(MicroserviceAuthenticationFilter)
服务间: Feign → /internal/** + X-Internal-Token（不经网关）
```

权限策略说明见 [docs/security/permission-strategy.md](docs/security/permission-strategy.md)。

## 快速启动

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 Nacos、MySQL（宿主机端口 **3307**）、Redis、RabbitMQ（可选）。Nacos 控制台：http://localhost:8848/nacos（默认 nacos/nacos）。

**配置中心**：首次启动后执行 `.\nacos\import-config.ps1` 将配置发布到 Nacos，详见 [nacos/README.md](nacos/README.md)。

数据库初始化（MySQL 容器首次启动后，或手动执行）：

```bash
# 基础 RBAC（必需）
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/01_star_pivot.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_sys_user.sql

# 可选模块
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_gen_tables.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_monitor_job.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/sys_menu.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/gulimall/init_mall_all.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/gulimall/init_mall_menus.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_approval.sql
```

> 审批中心表结构及商城默认模板见 `sql/init_approval.sql`。忘记密码开关默认关闭，可在系统参数 `sys.account.forgetPassword` 中开启。

### 2. 编译项目

```bash
mvn clean install -DskipTests
```

### 3. 启动服务

**最小集（登录 + 系统管理）：**

```bash
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth
mvn spring-boot:run -pl starpivot-gateway
```

**完整功能（按需另开终端）：**

```bash
mvn spring-boot:run -pl starpivot-file
mvn spring-boot:run -pl starpivot-generator
mvn spring-boot:run -pl starpivot-monitor
mvn spring-boot:run -pl starpivot-mall
mvn spring-boot:run -pl starpivot-approval
```

默认账号 **admin / 123456**（来自 `sql/01_star_pivot.sql` 初始数据）。若登录提示密码错误，说明密码已在系统中修改，请使用实际密码或通过「个人中心 → 修改密码」重置。

### 4. 启动前端

```bash
cd star-pivot-ui
pnpm install
pnpm dev
```

前端开发服务器默认 http://localhost:3000，API 经 Vite 代理转发到网关 `http://localhost:8080`。

### 5. 验证

对外 API 统一前缀 **`/api/v1`**。前端经 Vite 将 `/api/auth/*` 改写为 `/api/v1/auth/*`，网关再转发至各微服务。

| 地址 | 说明 |
|------|------|
| POST http://localhost:8080/api/v1/auth/login | 登录（默认 admin / 123456；须先验证码接口获取 `captchaProof`） |
| GET http://localhost:8080/api/v1/auth/userinfo | 获取用户信息（需 Token） |
| GET http://localhost:8080/api/v1/health | 经网关访问系统服务健康检查（需 Token） |

请求体示例（登录，须先 `GET /api/v1/auth/captcha` + `POST /api/v1/auth/captcha/verify` 取得 `captchaProof`）：

```json
{
  "username": "admin",
  "password": "123456",
  "captchaProof": "<verify 接口返回的一次性凭证>"
}
```

登录后在请求头携带 `Authorization: Bearer <token>` 访问受保护接口。响应头会返回 `X-Trace-Id` 便于链路排查。

### 6. 审批端到端联调（可选）

完整链路：**采购提交审批 → 待办 → 审批通过 → MQ 回写 `audit_status`**。

**前置**：除最小集外另启 `starpivot-mall`、`starpivot-approval`；RabbitMQ 已启动且 `MQ_ENABLED=true`（见 Nacos `mq-config.yaml`）；已执行 `sql/init_approval.sql` 与商城 WMS 脚本。

```powershell
# 自动插入测试采购单并跑完全流程（默认密码 123456）
.\scripts\e2e-approval.ps1

# 指定已有采购单；密码若已修改
.\scripts\e2e-approval.ps1 -PurchaseId 3 -Password admin123 -ResetPurchase
```

脚本成功时输出 `E2E PASSED`，并校验 `wms_purchase.audit_status=APPROVED`。

## 环境变量

完整说明见 [nacos/README.md](nacos/README.md)。常用变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| NACOS_SERVER | 127.0.0.1:8848 | Nacos 地址 |
| API_VERSION | v1 | API 版本段，影响 context-path 与网关路由 |
| DB_URL | jdbc:mysql://127.0.0.1:3307/starpivot?... | 数据库连接（docker 映射端口 3307） |
| DB_USERNAME | root | 数据库用户 |
| DB_PASSWORD | root | 数据库密码 |
| REDIS_HOST | 127.0.0.1 | Redis 主机 |
| REDIS_PASSWORD | root | Redis 密码 |
| JWT_SECRET | （见 common-config） | JWT 签名密钥，至少 32 字符 |
| INTERNAL_SERVICE_TOKEN | （空） | 服务间 `/internal/**` 调用 Token |
| INTERNAL_SERVICE_TOKEN_REQUIRED | false | 为 true 时未配置 Token 将拒绝内部接口（生产建议 true） |
| MQ_ENABLED | false | 是否启用 RabbitMQ（审批完结回调、登录日志等） |

## 新增业务模块

1. 在根目录创建 `starpivot-xxx` 模块，继承父 POM
2. 在 `starpivot-cloud-dependencies/pom.xml` 中声明内部模块版本
3. 在 `starpivot-gateway` 的 `application.yml` 中添加路由规则
4. 在根 `pom.xml` 的 `<modules>` 中注册新模块
5. 在 `nacos/config/` 添加对应 `starpivot-xxx.yaml` 并执行 import 脚本
6. 按 [权限策略文档](docs/security/permission-strategy.md) 选择 `authority-strategy`

## 可观测性

详见 [docs/observability.md](docs/observability.md)。概要：

- **TraceId**：网关与各 Servlet 微服务自动生成/透传 `X-Trace-Id`，日志格式含 `%X{traceId}`
- **Actuator**：各服务暴露 `health`、`info`、`metrics`（见 `common-config.yaml`）
- **Feign**：内部调用自动透传 TraceId 与 `X-Internal-Token`

## 相关文档

- [审批中心设计方案](Java微服务版本审批中心设计方案.md)
- [审批开发规格](docs/doc/workflow-design.md)
- [MQ 使用说明](docs/doc/mq-usage.md)
