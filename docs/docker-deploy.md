# StarPivot Docker 部署指南

本地开发采用 **混合模式**：中间件在 Docker 中运行，微服务与前端在宿主机通过 `mvn` / `pnpm` 启动。

---

## 文件结构

```
docker-compose.yml        # 基础设施：Nacos / MySQL / Redis / RabbitMQ / Zipkin
.env.local                # 本地开发参考（默认即 127.0.0.1，一般不用 export）
```

---

## 前置条件

- Docker Desktop（建议分配 **≥ 2GB** 内存给 Docker）
- JDK 17+、Maven 3.8+、Node.js ≥ 20.19、pnpm ≥ 8.8

---

## 混合开发（推荐）

只启动中间件，业务服务在宿主机运行。

```powershell
# 1. 基础设施
docker compose up -d

# 2. 导入 Nacos 配置（首次或配置变更后）
$env:JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
.\nacos\import-config.ps1

# 3. 编译 & 启动微服务（各开终端）
mvn clean install -DskipTests
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth
mvn spring-boot:run -pl starpivot-gateway
# ... 按需启动其他模块

# 4. 前端
cd star-pivot-ui
pnpm install
pnpm dev
```

访问：http://localhost:3000（前端）→ 代理到 http://localhost:8080（网关）

`.env.local` 仅作参考；`application.yml` 与 Nacos 默认值已指向 `127.0.0.1:3307` 等映射端口。

---

## 中间件访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:8848/nacos | Nacos 控制台（nacos/nacos） |
| localhost:3307 | MySQL（容器内 3306，库名 `star_pivot`） |
| localhost:6379 | Redis（密码 `root`） |
| http://localhost:15672 | RabbitMQ 管理台（admin/admin） |
| http://localhost:9411 | Zipkin |

默认账号：**admin / 123456**（若已导入 `sql/star_pivot.sql`）

---

## 常用命令

```powershell
# 查看状态
docker compose ps

# 停止（保留数据卷）
docker compose down

# 停止并清空数据库（慎用）
docker compose down -v

# 仅启动某个中间件
docker compose up -d rabbitmq
```

---

## 故障排查

1. **微服务连不上数据库**  
   确认 `docker compose ps` 中 MySQL、Redis、Nacos 均为 healthy；首次启动 MySQL 初始化可能需要 1～2 分钟。

2. **Nacos 无配置**  
   手动执行 `.\nacos\import-config.ps1`。

3. **已有 MySQL 数据卷但缺少 star_pivot_mall 库**  
   手动执行：`CREATE DATABASE star_pivot_mall ...` 后导入 `sql/star_pivot_mall.sql`。

4. **auth 出现「回退 Feign」**  
   确认 RabbitMQ 已启动：`docker compose up -d rabbitmq`。
