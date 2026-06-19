# starpivot-cloud

星枢微服务项目 — Spring Cloud Alibaba 微服务骨架。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.5.9 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos | 2.3.x |
| MyBatis-Plus | 3.5.12 |

## 模块结构

```
starpivot-cloud/
├── starpivot-cloud-dependencies/   # BOM 依赖版本管理
├── starpivot-common/               # 公共实体、JWT 工具、异常处理
├── starpivot-api/                  # Feign 接口与跨服务 DTO
├── starpivot-gateway/              # API 网关 (8080)
├── starpivot-auth/                 # 认证授权服务 (9200)
└── starpivot-system/               # 系统管理服务 (9201)
```

## 鉴权流程

```
登录: Client → Gateway → Auth → Feign → System(/internal/user) → MySQL
业务: Client → Gateway(JWT 校验) → System(携带 X-User-* 请求头)
```

## 快速启动

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 Nacos、MySQL、Redis。Nacos 控制台：http://localhost:8848/nacos（默认 nacos/nacos）。

首次使用前，在 MySQL 中手动执行初始化脚本：

```bash
mysql -h127.0.0.1 -uroot -proot starpivot < sql/init_sys_user.sql
```

### 2. 编译项目

```bash
mvn clean install -DskipTests
```

### 3. 启动服务

```bash
# 认证服务（依赖 system 已注册到 Nacos）
mvn spring-boot:run -pl starpivot-system
mvn spring-boot:run -pl starpivot-auth

# 网关（另开终端）
mvn spring-boot:run -pl starpivot-gateway
```

默认账号 admin / 123456（见 `sql/init_sys_user.sql`）。

### 5. 启动前端

```bash
cd star-pivot-ui
pnpm install
pnpm dev
```

前端开发服务器默认 http://localhost:3000 ，API 经 Vite 代理转发到网关 `http://localhost:8080`。

### 4. 验证

| 地址 | 说明 |
|------|------|
| POST http://localhost:8080/auth/login | 登录（admin / 123456） |
| GET http://localhost:8080/auth/user/info | 获取用户信息（需 Token） |
| GET http://localhost:8080/system/health | 经网关访问系统服务（需 Token） |
| http://localhost:9200/doc.html | Auth 服务接口文档 |

登录后在请求头携带 `Authorization: Bearer <token>` 访问受保护接口。

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| NACOS_SERVER | 127.0.0.1:8848 | Nacos 地址 |
| MYSQL_HOST | 127.0.0.1 | MySQL 主机 |
| MYSQL_PORT | 3306 | MySQL 端口 |
| MYSQL_DB | starpivot | 数据库名 |
| MYSQL_USER | root | 数据库用户 |
| MYSQL_PASSWORD | root | 数据库密码 |
| REDIS_HOST | 127.0.0.1 | Redis 主机 |
| REDIS_PORT | 6379 | Redis 端口 |
| JWT_SECRET | starpivot-cloud-jwt-secret-key-min-32-chars | JWT 签名密钥 |
| JWT_EXPIRE | 7200000 | Token 有效期（毫秒） |

## 新增业务模块

1. 在根目录创建 `starpivot-xxx` 模块，继承父 POM
2. 在 `starpivot-cloud-dependencies/pom.xml` 中声明内部模块版本
3. 在 `starpivot-gateway` 的 `application.yml` 中添加路由规则
4. 在根 `pom.xml` 的 `<modules>` 中注册新模块
