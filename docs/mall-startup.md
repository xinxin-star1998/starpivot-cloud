# 商城微服务启动指南

商城已按 Guli 风格拆分为 **5 个业务微服务 + 1 个静态资源 BFF**，共用 Nacos 服务发现与 `star_pivot_mall` 数据库。

## 架构与端口

| 服务 | Nacos 名称 | 默认端口 | 职责 |
|------|------------|----------|------|
| starpivot-mall-member | `starpivot-mall-member` | 9206 | 会员、认证、地址、收藏 |
| starpivot-mall-product | `starpivot-mall-product` | 9207 | 商品 PMS、ES 搜索、C 端商品/评价 |
| starpivot-mall-ware | `starpivot-mall-ware` | 9208 | 仓储、采购、省市区 |
| starpivot-mall-order | `starpivot-mall-order` | 9209 | 订单、购物车、支付 |
| starpivot-mall-promotion | `starpivot-mall-promotion` | 9212 | 优惠券、秒杀、首页营销 |
| starpivot-mall | `starpivot-mall` | 9205 | 本地静态资源 `/local-storage/**`（可选） |

网关 `starpivot-gateway`（8080）按路径转发至上述服务，详见 `starpivot-gateway/src/main/resources/application.yml`。

### 服务间 Feign（内部 `/internal/**`）

```
order     → member, product, ware, promotion
member    → order
product   → order（可售库存）, ware（初始化 SKU 库存）
ware      → order, product
promotion → member, product, order（购物车/秒杀下单）
```

## 前置条件

1. **基础设施**：`docker compose up -d`（Nacos、MySQL、Redis、RabbitMQ）
2. **商城库数据**（首次）：

```bash
mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_mall < sql/star_pivot_mall.sql
```

3. **环境变量**（PowerShell 示例）：

```powershell
$env:JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars"
$env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"
$env:MALL_DB_URL = "jdbc:mysql://127.0.0.1:3307/star_pivot_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
```

4. **编译**：

```bash
mvn clean install -DskipTests
```

## Nacos 配置导入

```powershell
# 全部 yaml
.\nacos\import-config.ps1

# 仅商城（common + oss + mq + starpivot-mall*）
.\nacos\import-config.ps1 -Profile Mall

# 不含商城
.\nacos\import-config.ps1 -Profile Core
```

Linux / macOS：`pwsh ./nacos/import-config.ps1 -Profile Mall`，或在 Nacos 控制台手动粘贴。

发布顺序（脚本按文件名排序，等价于）：`common-config.yaml` → `oss-config.yaml` → `mq-config.yaml` → `starpivot-mall*.yaml`。

单文件更新：

```powershell
.\nacos\upload-config.ps1 starpivot-mall-order.yaml
```

配置说明见 [nacos/README.md](../nacos/README.md)。

### 商城 Nacos Data ID

| Data ID | 说明 |
|---------|------|
| `common-config.yaml` | Redis、JWT、内部 Token（**必选**） |
| `oss-config.yaml` | 阿里云 OSS（product、promotion 等共用，**商品图需启用**） |
| `mq-config.yaml` | RabbitMQ（审批回调、营销 MQ） |
| `starpivot-mall.yaml` | 静态资源本地路径（默认 `oss.enabled=false` 覆盖公共配置） |
| `starpivot-mall-member.yaml` | 会员库、短信/微信登录 |
| `starpivot-mall-product.yaml` | 商品库、Elasticsearch |
| `starpivot-mall-ware.yaml` | 仓储库 |
| `starpivot-mall-order.yaml` | 订单库、支付（微信/支付宝）、Mock 支付 |
| `starpivot-mall-promotion.yaml` | 营销库、秒杀 Redis |

控制台：http://localhost:8848/nacos（`nacos` / `nacos`）

## 启动顺序

Feign 跨服务调用要求 **被调方先注册到 Nacos**。推荐顺序：

```
1. gateway（8080）— 对外入口，可与其他服务并行，但验证 API 时需已启动
2. member (9206)
3. product (9207)
4. ware (9208)
5. promotion (9212)
6. order (9209)     — 依赖 promotion / product / ware / member
7. mall-app (9205)  — 可选，仅 /local-storage/**
```

平台基础服务（开发联调常用）：`system` → `auth` → `gateway` → `approval`（**9213**，与 member 9206 错开）。

### 手动启动（各开终端）

```bash
mvn spring-boot:run -pl starpivot-gateway
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-member -am
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-product -am
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-ware -am
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-promotion -am
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-order -am
# 可选
mvn spring-boot:run -pl starpivot-mall/starpivot-mall-app -am
```

指定端口：`SERVER_PORT=9207 mvn spring-boot:run -pl starpivot-mall/starpivot-mall-product -am`

**网关需单独启动**：`mvn spring-boot:run -pl starpivot-gateway`

## 验证

1. Nacos「服务管理 → 服务列表」应出现 5～6 个 `starpivot-mall-*` 实例（healthy）。
2. 经网关访问（需 `auth` + 会员 token 的接口按业务要求携带 JWT）：
   - 首页：`GET http://localhost:8080/api/v1/portal/home/index`
   - 商品：`GET http://localhost:8080/api/v1/portal/product/...`
   - 省市区：`GET http://localhost:8080/api/v1/portal/region/children?parentId=0`
3. Knife4j（直连服务）：`http://localhost:9207/api/v1/doc.html`（product 示例）

## 网关路由速查

| 路径前缀 | 目标服务 |
|----------|----------|
| `/portal/member/**`, `/portal/auth/**`, `/portal/address/**`, `/portal/collect/**` | member |
| `/portal/product/**`, `/portal/image/**`, `/portal/comment/**` | product |
| `/portal/region/**`, `/mall/wareinfo/**`, `/mall/purchase/**` 等 | ware |
| `/portal/order/**`, `/portal/cart/**`, `/portal/pay/**`, `/mall/order/**` 等 | order |
| `/portal/home/**`, `/portal/coupon/**`, `/portal/seckill/**`, `/mall/coupon/**` 等 | promotion |
| `/local-storage/**` | starpivot-mall (9205) |

完整 B 端 `/mall/**` 菜单映射见 [mall.md](mall.md)。

## 常见问题

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| Feign 503 / 服务不可用 | 被调服务未启动或未注册 Nacos | 按启动顺序检查 Nacos 服务列表 |
| 内部接口 401 | `INTERNAL_SERVICE_TOKEN` 不一致 | 统一 env 与 `common-config.yaml` 后 re-import |
| 下单/计价失败 | promotion 或 product 未起 | 先起 promotion、product，再起 order |
| 支付回调失败 | `MALL_PUBLIC_BASE_URL` 未指向网关 | 在 `starpivot-mall-order.yaml` 配置为 `http://localhost:8080` |
| 审批状态未回写 | MQ 未启用、RabbitMQ 未启动或对应商城服务未起 | `MQ_ENABLED=true`，`docker compose up -d rabbitmq`，确认已 import `mq-config.yaml`，并启动 ware/product/order/promotion 中对应服务 |
| 端口被占用（9206） | approval 与 member 同为 9206 | approval 默认 **9213**；member 固定 **9206**；IntelliJ 中勿给两者设相同 `SERVER_PORT` |
| 端口被占用（9210/9211） | Windows 上 QQ 常占用 9210、9211 | promotion 默认 **9212** |

## Maven 模块

```
starpivot-mall-parent
├── starpivot-mall-common
├── starpivot-mall-member
├── starpivot-mall-product
├── starpivot-mall-ware
├── starpivot-mall-order
├── starpivot-mall-promotion
└── starpivot-mall-app          # artifactId: starpivot-mall
```

一次性编译商城全部模块：

```bash
mvn compile -pl starpivot-mall/starpivot-mall-member,starpivot-mall/starpivot-mall-product,starpivot-mall/starpivot-mall-ware,starpivot-mall/starpivot-mall-order,starpivot-mall/starpivot-mall-promotion,starpivot-mall/starpivot-mall-app -am
```
