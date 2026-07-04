# Nacos 配置中心

StarPivot 微服务配置统一托管在 Nacos，本地 `application.yml` 仅保留端口、路由等启动必需项。

## 配置结构

| Data ID | 说明 |
|---------|------|
| `common-config.yaml` | 公共配置：Redis、JWT、内部 Token、日志、MQ 开关 |
| `oss-config.yaml` | 阿里云 OSS（system、file、product、promotion、mall-app 共用） |
| `mq-config.yaml` | RabbitMQ（审批/MQ 消费者） |
| `starpivot-gateway.yaml` | 网关 Redis、日志 |
| `starpivot-auth.yaml` | 认证服务 Redis、健康检查 |
| `starpivot-system.yaml` | 数据源、Druid |
| `starpivot-file.yaml` | 数据源、文件中心 |
| `starpivot-generator.yaml` | 数据源、代码生成参数 |
| `starpivot-monitor.yaml` | 数据源、Druid 监控、Quartz |
| `starpivot-approval.yaml` | 审批服务 |
| `starpivot-mall.yaml` | 商城静态资源 BFF（本地磁盘路径，默认 `oss.enabled=false`） |
| `starpivot-mall-member.yaml` | 会员服务：数据源、短信/微信登录 |
| `starpivot-mall-product.yaml` | 商品服务：数据源、ES |
| `starpivot-mall-ware.yaml` | 仓储服务：数据源 |
| `starpivot-mall-order.yaml` | 订单服务：数据源、支付配置 |
| `starpivot-mall-promotion.yaml` | 营销服务：数据源、秒杀 |

Group 默认：`DEFAULT_GROUP`

## 导入脚本

| 脚本 | 说明 |
|------|------|
| `import-config.ps1` | 批量发布 `nacos/config/*.yaml` |
| `import-config.ps1 -Profile Mall` | 仅 common + oss + mq + `starpivot-mall*` |
| `import-config.ps1 -Profile Core` | 不含 `starpivot-mall*` |
| `upload-config.ps1 <file>` | 发布单个 yaml |

Linux / macOS 可安装 [PowerShell](https://learn.microsoft.com/powershell/) 后执行 `pwsh ./nacos/import-config.ps1`，或在 Nacos 控制台手动粘贴配置。

商城启动顺序、Feign 依赖、网关路由详见 [docs/mall-startup.md](../docs/mall-startup.md)。

## 快速导入

1. 启动基础设施：

```bash
docker compose up -d
```

2. 设置必需环境变量（至少 JWT 密钥）：

```powershell
$env:JWT_SECRET = "your-secret-at-least-32-characters-long"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
```

3. 发布配置到 Nacos：

```powershell
# 全部
.\nacos\import-config.ps1

# 仅商城微服务
.\nacos\import-config.ps1 -Profile Mall
```

4. 打开 Nacos 控制台确认：http://localhost:8848/nacos（默认账号 `nacos` / `nacos`）

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `NACOS_SERVER` | Nacos 地址 | `127.0.0.1:8848` |
| `NACOS_GROUP` | 配置分组 | `DEFAULT_GROUP` |
| `NACOS_NAMESPACE` | 命名空间 ID（空=public） | 空 |
| `NACOS_USERNAME` | 控制台账号 | `nacos` |
| `NACOS_PASSWORD` | 控制台密码 | `nacos` |
| `JWT_SECRET` | JWT 签名密钥（≥32 字符） | **必填** |
| `INTERNAL_SERVICE_TOKEN` | 服务间内部 API Token | **本地/生产均建议设置** |
| `INTERNAL_SERVICE_TOKEN_REQUIRED` | 未配置 Token 时是否拒绝内部接口 | `true` |
| `TRUST_GATEWAY_HEADERS` | 微服务是否信任网关透传身份 Header | `false`（生产保持 false） |
| `REDIS_PASSWORD` | Redis 密码 | `root`（与 docker-compose 一致） |
| `MQ_ENABLED` | 是否启用 RabbitMQ | `false` |
| `OSS_ENABLED` | 是否启用 OSS | `true`（`oss-config.yaml`）；mall-app 默认 `false` |
| `OSS_ENDPOINT` | OSS 端点 | `oss-cn-beijing.aliyuncs.com` |
| `OSS_ACCESS_KEY_ID` | OSS AccessKey | 空（通过环境变量注入） |
| `OSS_ACCESS_KEY_SECRET` | OSS AccessKey Secret | 空 |
| `OSS_BUCKET_NAME` | OSS 桶名 | `star-pivot` |
| `OSS_URL_PREFIX` | OSS 访问前缀（CDN 域名等） | 空 |
| `DB_URL` | 数据库连接 | 见各服务 yaml |

## 加载机制

各服务 `application.yml` 通过 Spring Boot 3 配置导入：

```yaml
spring:
  config:
    import:
      - optional:nacos:common-config.yaml
      - optional:nacos:oss-config.yaml
      - optional:nacos:${spring.application.name}.yaml
```

- Nacos 配置**覆盖**本地同名属性（如 `starpivot-mall.yaml` 中 `oss.enabled=false` 覆盖公共 `oss-config.yaml`）
- `optional:` 表示 Nacos 不可用时仍可用本地 fallback 启动
- 修改 Nacos 配置后支持热刷新（`refreshEnabled=true`）

## 生产建议

1. 创建独立 Nacos 命名空间（如 `prod`），设置 `NACOS_NAMESPACE`
2. 敏感项（`JWT_SECRET`、`OSS_*`、`INTERNAL_SERVICE_TOKEN`）通过 K8s Secret / 环境变量注入，Nacos 中只写 `${JWT_SECRET}` 占位
3. 去掉 `optional:` 前缀，强制从 Nacos 加载配置
