# StarPivot 商城菜单与接口对照

> 商城已拆分为 5 个领域微服务，启动与端口见 [mall-startup.md](./mall-startup.md)。  
> 详细表设计与开发进度见 [商城开发事项](./商城开发事项.md)

## 服务信息

| 项 | 值 |
|---|---|
| 微服务 | `starpivot-mall-member` (9206)、`product` (9207)、`ware` (9208)、`order` (9209)、`promotion` (9212) |
| 统一入口 | 网关 `starpivot-gateway:8080` |
| API 前缀 | `/api/v1`（环境变量 `API_VERSION`） |
| 业务库 | 五域分库：`star_pivot_product` / `star_pivot_ware` / `star_pivot_order` / `star_pivot_member` / `star_pivot_promotion` |
| 系统库 | `star_pivot`（菜单/权限） |
| 系统库名配置 | 商城服务通过 `SYSTEM_DB_SCHEMA` 跨库读 RBAC，**须与 system 服务库名一致**（推荐 `star_pivot`） |
| 权限策略 | `menu-permission`（见 `docs/security/permission-strategy.md`） |
| 接口文档 | 经网关：`http://localhost:8080/api/v1/doc.html`；或直连各服务 `http://localhost:{port}/api/v1/doc.html` |

> B 端 `/api/v1/mall/...` 与 C 端 `/api/v1/portal/...` 由网关按路径转发至对应微服务。  
> 后台前端 `star-pivot-ui/src/views/mall/` 与 C 端 `views/portal/` **均已对接**。

## 模块划分（按微服务）

| 前缀 | 模块 | 微服务 | Java 包 | 说明 |
|---|---|---|---|---|
| portal / ums | C 端 / 会员 | member | `mall.portal` / `mall.ums` | 首页入口、会员、地址、收藏 |
| pms | 商品系统 | product | `mall.pms` | 分类、品牌、属性、SPU/SKU、评论 |
| wms | 仓储系统 | ware | `mall.wms` | 仓库、库存、地区、采购、工作单 |
| oms | 订单系统 | order | `mall.oms` | 订单、支付、退货、购物车 |
| sms | 营销中心 | promotion | `mall.sms` | 优惠券、专题、秒杀、满减/阶梯价、轮播 |

## 菜单对照（以 `sql/sys_menu.sql` 为准）

> 组件路径 = 前端 `views` 下路径（不含 `.vue`）。  
> 菜单 ID 以 `sql/sys_menu.sql` / `sql/star_pivot.sql` 为准（商城父菜单 **5**，子模块 **29~34**）。

### 商品系统 PMS（parent 29）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 126 | 分类维护 | `/mall/pms/category/index` | `/mall/category` | ✅ |
| 127 | 品牌列表 | `/mall/pms/brand/index` | `/mall/brand` | ✅ |
| 138 | 属性分组 | `/mall/pms/group/index` | `/mall/group` | ✅ |
| 139 | 规格参数 | `/mall/pms/attr/base/index` | `/mall/attr` | ✅ |
| 140 | 销售属性 | `/mall/pms/attr/sale/index` | `/mall/attr` | ✅ |
| 141 | spu管理 | `/mall/pms/product/spu/index` | `/mall/product` | ✅ SPU 列表 + 分类树 |
| 142 | 发布商品 | `/mall/pms/product/publish/index` | — | ✅ 跳转 `/mall/product/add` 发布向导 |
| 143 | 商品管理 | `/mall/pms/product/manager/index` | `/mall/sku` | ✅ SKU 只读检索 |
| 144 | SKU 管理 | `/mall/pms/sku/index` | `/mall/sku` | 已隐藏，与 143 合并 |

动态路由（无菜单）：`/mall/product/add`、`/mall/product/edit/:id` → `pms/product/modules/addSpu`

### 仓储 WMS（parent 30）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 167 | 仓库维护 | `/mall/wms/warehouse/index` | `/mall/wareinfo` | ✅ |
| 168 | 地区管理 | `/mall/wms/address/index` | `/mall/address` | ✅ |
| 169 | 商品库存 | `/mall/wms/sku/index` | `/mall/ware-sku` | ✅ |
| 170 | 库存工作单 | `/mall/wms/task/index` | `/mall/ware-task` | ✅ |
| 189 | 采购需求 | `/mall/wms/purchaseitem/index` | `/mall/purchase/detail/*` | ✅ |
| 190 | 采购单 | `/mall/wms/purchaseorder/index` | `/mall/purchase/*` | ✅ |

### 优惠营销 SMS（parent 31）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 193 | 优惠券 | `/mall/sms/coupon/coupon/index` | `/mall/coupon` | ✅ |
| 194 | 发放记录 | `/mall/sms/coupon/history/index` | `/mall/coupon-history` | ✅ |
| 195 | 专题活动 | `/mall/sms/coupon/subject/index` | `/mall/subject` | ✅ |
| 196 | 秒杀活动 | `/mall/sms/coupon/seckill/index` | `/mall/seckill` | ✅ |
| 197 | 积分维护 | `/mall/sms/coupon/bounds/index` | `/mall/bounds` | ✅ |
| 198 | 满减折扣 | `/mall/sms/coupon/full/index` | `/mall/reduction` | ✅ |
| 199 | 会员价格 | `/mall/sms/coupon/memberprice/index` | `/mall/memberprice` | ✅ |
| 200 | 每日秒杀 | `/mall/sms/coupon/seckillsession/index` | `/mall/seckill-session` | ✅ |

### 订单 OMS（parent 32）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 227 | 订单查询 | `/mall/oms/order/order/index` | `/mall/order` | ✅ |
| 228 | 退货单处理 | `/mall/oms/order/return/index` | `/mall/order-return` | ✅ |
| 229 | 等级规则 | `/mall/oms/order/level/index` | `/mall/order-setting` | ✅ |
| 230 | 支付流水 | `/mall/oms/order/payment/index` | `/mall/payment` | ✅ |
| 231 | 退款流水 | `/mall/oms/order/refund/index` | `/mall/refund` | ✅ 含同步/重试/失败告警 |

退款流水扩展 API：`GET /mall/refund/{id}` · `PUT /mall/refund/{id}/sync` · `PUT /mall/refund/{id}/retry` · `GET /mall/refund/alert/summary` · `POST /mall/refund/{id}/ack-alert`

### 会员 UMS（parent 33）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 239 | 会员列表 | `/mall/ums/member/member/index` | `/mall/member` | ✅ |
| 240 | 会员等级 | `/mall/ums/member/level/index` | `/mall/member-level` | ✅ |
| 241 | 积分变化 | `/mall/ums/member/growth/index` | `/mall/member-growth` | ✅ |
| 242 | 统计信息 | `/mall/ums/member/statistics/index` | `/mall/member-statistics` | ✅ |
| 271 | 登录日志 | `/mall/ums/member/login-log/index` | `/mall/member-login-log` | ✅ |
| 272 | 会员收藏 | `/mall/ums/member/collect/index` | `/mall/member-collect` | ✅ |
| 317 | 收货地址 | `/mall/ums/member/receive-address/index` | `/mall/member-receive-address` | ✅ |

### 内容 CMS（parent 34）

| 菜单 ID | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 248 | 首页推荐 | `/mall/cms/content/home/index` | `/mall/home-adv` | ✅ |
| 249 | 分类热门 | `/mall/cms/content/category/index` | — | ✅（引导至专题活动） |
| 250 | 评论管理 | `/mall/cms/content/comments/index` | `/mall/comment` | ✅ |

## C 端 Portal

| 路由 | 页面 | API |
|---|---|---|
| `/portal` | 首页 | `/portal/home`、`/portal/product/search` |
| `/portal/search` | 搜索 | `/portal/product/search` |
| `/portal/product/:id` | 商品详情 | `/portal/product/{id}`、`/portal/comment` |
| `/portal/cart` | 购物车 | `/portal/cart` |
| `/portal/checkout` | 结算 | `/portal/order/submit`、`/portal/coupon` |
| `/portal/orders` | 我的订单 | `/portal/order/*` |
| `/portal/coupons` | 优惠券 | `/portal/coupon` |
| `/portal/seckill` | 秒杀 | `/portal/seckill` |
| `/portal/subject` | 专题 | `/portal/subject` |
| `/portal/account` | 个人中心 | `/portal/member` |
| `/portal/account/addresses` | 收货地址 | `/portal/address`、`/portal/region/children` |
| `/portal/account/favorites` | 我的收藏 | `/portal/collect` |
| `/portal/login` · `/portal/register` | 登录/注册 | `/portal/auth`、`/portal/member` |

前端 API：`star-pivot-ui/src/api/portal/` · 微信小程序 `star-pivot-mp/` 复用同一套 Portal API（见 [miniprogram.md](./miniprogram.md)）

## 数据库初始化

```bash
# 1. 系统库（含商城基础菜单 24~210、281）
mysql -uroot -p < sql/star_pivot.sql
mysql -uroot -p star_pivot < sql/sys_menu.sql

# 2. 商城五域业务库（Docker 首次启动会执行 00_create_mall_database.sql 建库）
.\sql\import_mall_databases.ps1

# 或逐库：
mysql -uroot -p star_pivot_product  < sql/star_pivot_product.sql
mysql -uroot -p star_pivot_ware     < sql/star_pivot_ware.sql
mysql -uroot -p star_pivot_order    < sql/star_pivot_order.sql
mysql -uroot -p star_pivot_member   < sql/star_pivot_member.sql
mysql -uroot -p star_pivot_promotion < sql/star_pivot_promotion.sql

# 3. 可选：仅刷新省市区（约 5.6 万条，省/市/区/乡镇）
mysql -uroot -p star_pivot_ware < sql/address.sql

# 4. 已有库补丁（退款失败告警字段）
mysql -uroot -p star_pivot_order < sql/patch_oms_refund_alert_ack.sql
```

## 实体代码生成

```bash
cd starpivot-mall/scripts
node gen_entities.js
```

模块映射：`pms_*` → pms，`oms_*` / `mq_message` → oms，`sms_*` → sms，`ums_*` → ums，`wms_*` / `address` → wms。
