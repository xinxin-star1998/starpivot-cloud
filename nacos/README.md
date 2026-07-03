# Nacos 配置中心

StarPivot 微服务配置统一托管在 Nacos，本地 `application.yml` 仅保留端口、路由等启动必需项。

## 配置结构

| Data ID | 说明 |
|---------|------|
| `common-config.yaml` | 公共配置：Redis、JWT、内部 Token、日志 |
| `starpivot-gateway.yaml` | 网关 Redis、日志 |
| `starpivot-auth.yaml` | 认证服务 Redis、健康检查 |
| `starpivot-system.yaml` | 数据源、OSS、Druid |
| `starpivot-file.yaml` | 数据源、OSS |
| `starpivot-generator.yaml` | 数据源、代码生成参数 |
| `starpivot-monitor.yaml` | 数据源、Druid 监控、Quartz |

Group 默认：`DEFAULT_GROUP`

## 快速导入

1. 启动基础设施：

```bash
docker compose up -d
```

2. 设置必需环境变量（至少 JWT 密钥）：

```powershell
# PowerShell
$env:JWT_SECRET = "your-secret-at-least-32-characters-long"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
```

3. 发布配置到 Nacos：

```powershell
# Windows
.\nacos\import-config.ps1

# Linux / macOS
chmod +x nacos/import-config.sh
./nacos/import-config.sh
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
| `DB_URL` | 数据库连接 | 见各服务 yaml |

## 加载机制

各服务 `application.yml` 通过 Spring Boot 3 配置导入：

```yaml
spring:
  config:
    import:
      - optional:nacos:common-config.yaml
      - optional:nacos:${spring.application.name}.yaml
```

- Nacos 配置**覆盖**本地同名属性
- `optional:` 表示 Nacos 不可用时仍可用本地 fallback 启动
- 修改 Nacos 配置后支持热刷新（`refresh-enabled: true`）

## 生产建议

1. 创建独立 Nacos 命名空间（如 `prod`），设置 `NACOS_NAMESPACE`
2. 敏感项（`JWT_SECRET`、`OSS_*`、`INTERNAL_SERVICE_TOKEN`）通过 K8s Secret / 环境变量注入，Nacos 中只写 `${JWT_SECRET}` 占位
3. 去掉 `optional:` 前缀，强制从 Nacos 加载配置
