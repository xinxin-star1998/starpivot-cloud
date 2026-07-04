# StarPivot 商城菜单与接口对照

> 商城已拆分为 5 个领域微服务，启动与端口见 [mall-startup.md](./mall-startup.md)。  
> 详细表设计与开发进度见 [商城开发事项](./商城开发事项.md)

## 服务信息

| 项 | 值 |
|---|---|
| 微服务 | `starpivot-mall-member` (9206)、`product` (9207)、`ware` (9208)、`order` (9209)、`promotion` (9212)、静态 BFF `starpivot-mall` (9205) |
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

## 菜单对照（以 `star_pivot.sql` 为准）

> 组件路径 = 前端 `views` 下路径（不含 `.vue`）。  
> 菜单 ID 以 `star_pivot.sql` 的 **180~210** 为准；`sys_menu.sql` 可补充 C 端/SKU/按钮权限。

### 商品系统 PMS（menu 24~31, 28, 281）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 25 | 分类维护 | `/mall/pms/category/index` | `/mall/category` | ✅ |
| 26 | 品牌列表 | `/mall/pms/brand/index` | `/mall/brand` | ✅ |
| 29~31 | 属性分组/规格/销售 | `/mall/pms/group/index` 等 | `/mall/group`、`/mall/attr` | ✅ |
| 141 | spu管理 | `/mall/pms/product/spu/index` | `/mall/product` | ✅ SPU 列表 + 分类树 |
| 142 | 发布商品 | `/mall/pms/product/publish/index` | — | ✅ 跳转 `/mall/product/add` 发布向导 |
| 143 | 商品管理 | `/mall/pms/product/manager/index` | `/mall/sku` | ✅ SKU 只读检索（无操作按钮） |
| 144 | ~~SKU 管理~~ | `/mall/pms/sku/index` | `/mall/sku` | 已隐藏，与 143 合并 |

动态路由（无菜单）：`/mall/product/add`、`/mall/product/edit/:id` → `pms/product/modules/addSpu`

### 仓储 WMS（menu 131~149, 183~186, 273）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 132 | 仓库维护 | `/mall/wms/warehouse/index` | `/mall/wareinfo` | ✅ |
| 133 | 地区管理 | `/mall/wms/address/index` | `/mall/address` | ✅ |
| 144 | 商品库存 | `/mall/wms/sku/index` | `/mall/ware-sku` | ✅ |
| 183 | 库存工作单 | `/mall/wms/task/index` | `/mall/ware-task` | ✅ |
| 185 | 采购需求 | `/mall/wms/purchaseitem/index` | `/mall/purchase/detail/*` | ✅ |
| 186 | 采购单 | `/mall/wms/purchaseorder/index` | `/mall/purchase/*` | ✅ |

### 优惠营销 SMS（menu 187~195）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 188 | 优惠券 | `/mall/sms/coupon/coupon/index` | `/mall/coupon` | ✅ |
| 189 | 发放记录 | `/mall/sms/coupon/history/index` | `/mall/coupon-history` | ✅ |
| 190~195 | 专题/秒杀/积分/满减/会员价 | `/mall/sms/coupon/*/index` | 见 [商城开发事项](./商城开发事项.md) | ✅ |

### 订单 OMS（menu 196~201）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 197 | 订单查询 | `/mall/oms/order/order/index` | `/mall/order` | ✅ |
| 198 | 退货处理 | `/mall/oms/order/return/index` | `/mall/order-return` | ✅ |
| 199 | 订单设置 | `/mall/oms/order/level/index` | `/mall/order-setting` | ✅ |
| 200 | 支付流水 | `/mall/oms/order/payment/index` | `/mall/payment` | ✅ |
| 201 | 退款流水 | `/mall/oms/order/refund/index` | `/mall/refund` | ✅ |

### 会员 UMS（menu 202~206）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 203 | 会员列表 | `/mall/ums/member/member/index` | `/mall/member` | ✅ |
| 204 | 会员等级 | `/mall/ums/member/level/index` | `/mall/member-level` | ✅ |
| 205 | 积分变化 | `/mall/ums/member/growth/index` | `/mall/member-growth` | ✅ |
| 206 | 统计信息 | `/mall/ums/member/statistics/index` | `/mall/member-statistics` | ✅ |

### 内容 CMS（menu 207~210）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 208 | 首页轮播 | `/mall/cms/content/home/index` | `/mall/home-adv` | ✅ |
| 209 | 分类热门 | `/mall/cms/content/category/index` | — | ✅（引导至专题活动） |
| 210 | 评论管理 | `/mall/cms/content/comments/index` | `/mall/comment` | ✅ |

## C 端 Portal

| 路由 | 页面 | API |
|---|---|---|
| `/portal` | 首页 | `/portal/home`、`/portal/product/search` |
| `/portal/product/:id` | 商品详情 | `/portal/product/{id}` |
| `/portal/cart` | 购物车 | `/portal/cart` |
| `/portal/checkout` | 结算 | `/portal/order/submit` |
| `/portal/orders` | 我的订单 | `/portal/order/*` |
| `/portal/login` | 登录/注册 | `/portal/member` |

前端 API：`star-pivot-ui/src/api/portal/`

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
```

## 实体代码生成

```bash
cd starpivot-mall/scripts
node gen_entities.js
```

模块映射：`pms_*` → pms，`oms_*` / `mq_message` → oms，`sms_*` → sms，`ums_*` → ums，`wms_*` / `address` → wms。
