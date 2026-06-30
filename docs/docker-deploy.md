# StarPivot Docker 部署指南

支持两种并行模式：

| 模式 | 适用场景 |
|------|----------|
| **混合模式** | 中间件在 Docker，微服务/前端本地 `mvn` / `pnpm` 启动（日常开发） |
| **全 Docker 模式** | 基础设施 + 全部微服务 + 前端均容器化（联调、演示、验收） |

业务 Java/Vue 代码无需修改，通过 **分层 compose 文件** 与 **环境变量** 切换连接地址。

---

## 文件结构

```
docker-compose.yml              # 基础设施：Nacos / MySQL / Redis / RabbitMQ / Zipkin
docker-compose.services.yml     # 8 个微服务 + Nacos 配置初始化
docker-compose.ui.yml           # 前端 Nginx
.env.docker                     # 容器内连接地址（mysql、redis、nacos 等服务名）
.env.local                      # 本地开发参考（默认即 127.0.0.1，一般不用 export）
docker/Dockerfile.service       # 通用 Java 多阶段构建
docker/Dockerfile.ui            # 前端 build + Nginx
scripts/docker-up.ps1           # Windows 一键启动脚本
scripts/docker-up.sh            # Linux / macOS 一键启动脚本
```

---

## 前置条件

- Docker Desktop（建议分配 **≥ 8GB** 内存给 Docker，全模块约需 6GB+）
- 全 Docker 首次构建需下载 Maven / Node 依赖，耗时较长

---

## 模式 A：混合开发（推荐日常）

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

## 模式 B：全 Docker 部署

### 一键启动（推荐）

```powershell
# Windows
.\scripts\docker-up.ps1 -Mode full

# Linux / macOS
chmod +x scripts/docker-up.sh docker/nacos-import.sh
./scripts/docker-up.sh full
```

### 分层手动启动

```powershell
# 仅基础设施
docker compose up -d

# 基础设施 + 全部微服务（含 Nacos 配置自动导入）
docker compose -f docker-compose.yml -f docker-compose.services.yml up -d --build

# 全套（含前端）
docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.ui.yml up -d --build
```

### 只启动部分微服务

```powershell
docker compose -f docker-compose.yml -f docker-compose.services.yml up -d starpivot-system starpivot-auth starpivot-gateway
```

### 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（Nginx，全 Docker 模式） |
| http://localhost:8080 | API 网关 |
| http://localhost:8848/nacos | Nacos 控制台（nacos/nacos） |
| http://localhost:15672 | RabbitMQ 管理台（admin/admin） |
| http://localhost:9411 | Zipkin |

默认账号：**admin / 123456**

---

## 启动顺序说明

```
Nacos / MySQL / Redis / RabbitMQ（健康检查就绪）
        ↓
nacos-init（一次性导入 nacos/config/*.yaml）
        ↓
starpivot-system → starpivot-auth → starpivot-gateway
        ↓
file / generator / monitor / mall / approval（并行）
        ↓
star-pivot-ui（依赖 gateway）
```

首次启动 MySQL 会自动执行 `sql/` 下脚本（含 `00_create_mall_database.sql` 创建商城库）。

---

## 环境变量

容器内使用 `.env.docker`（已通过 compose `env_file` 注入），主要差异：

| 变量 | 本地默认 | Docker 容器内 |
|------|----------|---------------|
| `NACOS_SERVER` | 127.0.0.1:8848 | nacos:8848 |
| `DB_URL` | 127.0.0.1:**3307** | mysql:**3306** |
| `REDIS_HOST` | 127.0.0.1 | redis |
| `RABBITMQ_HOST` | 127.0.0.1 | rabbitmq |
| `MQ_ENABLED` | false | true |

生产环境请修改 `.env.docker` 中的 `JWT_SECRET`、`INTERNAL_SERVICE_TOKEN` 等敏感项。

---

## 常用命令

```powershell
# 查看状态
docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.ui.yml ps

# 查看某服务日志
docker logs -f starpivot-gateway

# 停止（保留数据卷）
docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.ui.yml down

# 停止并清空数据库（慎用）
docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.ui.yml down -v

# 仅重建某个微服务
docker compose -f docker-compose.yml -f docker-compose.services.yml up -d --build starpivot-mall
```

---

## 单独构建镜像

```powershell
# 构建单个微服务镜像
docker build -f docker/Dockerfile.service --build-arg MODULE=starpivot-gateway -t starpivot-gateway:local .

# 构建前端镜像
docker build -f docker/Dockerfile.ui -t star-pivot-ui:local .
```

---

## 故障排查

1. **微服务启动失败 / 连不上数据库**  
   确认 `docker compose ps` 中 MySQL、Redis、Nacos 均为 healthy；首次启动 MySQL 初始化可能需要 1～2 分钟。

2. **Nacos 无配置**  
   检查 `starpivot-nacos-init` 容器是否成功退出：`docker logs starpivot-nacos-init`  
   也可手动执行 `.\nacos\import-config.ps1`。

3. **网关 503 / 找不到服务**  
   等待各微服务注册到 Nacos（启动后约 1～2 分钟），或查看 `docker logs starpivot-gateway`。

4. **商城模块启动慢**  
   `starpivot-mall` 依赖较大，`start_period` 设为 180s，首次请耐心等待。

5. **已有 MySQL 数据卷但缺少 star_pivot_mall 库**  
   手动执行：`CREATE DATABASE star_pivot_mall ...` 后导入 `sql/star_pivot_mall.sql`。

6. **全 Docker 与本地开发端口冲突**  
   不要同时用 `mvn spring-boot:run` 和 Docker 微服务映射同一端口（如 8080）。

---

## 资源建议

| 范围 | 内存 |
|------|------|
| 仅基础设施 | ~2GB |
| 基础设施 + 最小三服务 | ~4GB |
| 全模块 + 前端 | ~8GB |
