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
├── starpivot-gateway/              # API 网关 (8080)
├── starpivot-auth/                 # 认证授权服务 (8081)
├── starpivot-system/               # 系统管理服务 (8082)
├── starpivot-file/                 # 文件服务 (9202)
├── starpivot-generator/            # 代码生成 (9203)
├── starpivot-monitor/              # 监控与定时任务 (9204)
└── star-pivot-ui/                  # Vue 前端
```

## 鉴权流程

```
登录: Client → Gateway → Auth → Feign → System(/internal/user) → MySQL
业务: Client → Gateway(JWT 校验) → 各微服务(MicroserviceAuthenticationFilter)
```

## 快速启动

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 Nacos、MySQL（宿主机端口 **3307**）、Redis。Nacos 控制台：http://localhost:8848/nacos（默认 nacos/nacos）。

**配置中心**：首次启动后执行 `.\nacos\import-config.ps1` 将配置发布到 Nacos，详见 [nacos/README.md](nacos/README.md)。

数据库初始化（MySQL 容器首次启动后，或手动执行）：

```bash
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_sys_user.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_gen_tables.sql
mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_monitor_job.sql
```

> 需先具备完整 RBAC 表结构（`sys_user` 等）；`init_sys_user.sql` 用于重置 admin 密码。

### 2. 编译项目

```bash
mvn clean install -DskipTests
```

### 3. 启动服务

```bash
# 先启动 system，再 auth
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth

# 网关（另开终端）
mvn spring-boot:run -pl starpivot-gateway
```

默认账号 **admin / 123456**。

### 4. 启动前端

```bash
cd star-pivot-ui
pnpm install
pnpm dev
```

前端开发服务器默认 http://localhost:3000，API 经 Vite 代理转发到网关 `http://localhost:8080`。

### 5. 验证

| 地址 | 说明 |
|------|------|
| POST http://localhost:8080/auth/login | 登录（admin / 123456） |
| GET http://localhost:8080/auth/user/info | 获取用户信息（需 Token） |
| GET http://localhost:8080/system/health | 经网关访问系统服务（需 Token） |

登录后在请求头携带 `Authorization: Bearer <token>` 访问受保护接口。

## 环境变量

完整说明见 [nacos/README.md](nacos/README.md)。常用变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| NACOS_SERVER | 127.0.0.1:8848 | Nacos 地址 |
| DB_URL | jdbc:mysql://127.0.0.1:3307/starpivot?... | 数据库连接（docker 映射端口 3307） |
| DB_USERNAME | root | 数据库用户 |
| DB_PASSWORD | root | 数据库密码 |
| REDIS_HOST | 127.0.0.1 | Redis 主机 |
| REDIS_PASSWORD | root | Redis 密码 |
| JWT_SECRET | （见 common-config） | JWT 签名密钥，至少 32 字符 |

## 新增业务模块

1. 在根目录创建 `starpivot-xxx` 模块，继承父 POM
2. 在 `starpivot-cloud-dependencies/pom.xml` 中声明内部模块版本
3. 在 `starpivot-gateway` 的 `application.yml` 中添加路由规则
4. 在根 `pom.xml` 的 `<modules>` 中注册新模块
5. 在 `nacos/config/` 添加对应 `starpivot-xxx.yaml` 并执行 import 脚本
