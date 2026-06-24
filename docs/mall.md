# StarPivot 商城菜单与接口对照

> 对应微服务：`starpivot-mall`  
> 详细表设计与开发进度见 [商城开发事项](./商城开发事项.md)

## 服务信息

| 项 | 值 |
|---|---|
| 服务名 | `starpivot-mall` |
| 端口 | `9205`（环境变量 `SERVER_PORT`） |
| API 前缀 | `/api/v1`（环境变量 `API_VERSION`） |
| 业务库 | `star_pivot_mall` |
| 系统库 | `star_pivot`（菜单/权限） |
| 系统库名配置 | mall 通过 `SYSTEM_DB_SCHEMA` 跨库读 RBAC，**须与 system 服务库名一致**（本地常见 `starpivot`，Docker 多为 `star_pivot`） |
| 权限策略 | `menu-permission`（见 `docs/security/permission-strategy.md`） |
| 接口文档 | `http://localhost:9205/api/v1/doc.html` |

> 当前已实现 **39** 个 Controller（B 端 `/api/v1/mall/...` + C 端 `/api/v1/portal/...`）。  
> 后台前端 `star-pivot-ui/src/views/mall/` 与 C 端 `views/portal/` **均已对接**。

## 模块划分

| 前缀 | 模块 | Java 包 | 说明 |
|---|---|---|---|
| portal | C 端商城 | `mall.portal` | 首页、商品、会员、购物车、地址、下单 |
| pms | 商品系统 | `mall.pms` | 分类、品牌、属性、SPU/SKU、评论 |
| wms | 仓储系统 | `mall.wms` | 仓库、库存、地区、采购、工作单 |
| oms | 订单系统 | `mall.oms` | 订单、支付、退货、订单设置 |
| sms | 营销中心 | `mall.sms` | 优惠券、专题、秒杀、满减/阶梯价、会员价、轮播 |
| ums | 会员中心 | `mall.ums` | 会员、等级、积分成长、统计 |
| — | 内容运营 | `mall.sms` / `mall.pms` | 轮播在 sms；评论在 pms |

## 菜单对照（以 `star_pivot.sql` 为准）

> 组件路径 = 前端 `views` 下路径（不含 `.vue`）。  
> 菜单 ID 以 `star_pivot.sql` 的 **180~210** 为准；`init_mall_menus.sql` 仅补充 **280~303**（C 端/SKU/按钮权限），勿与 200~210 冲突。  
> 若曾执行旧版 `init_mall_menus.sql`，请依次执行 `migrate_mall_menus_v2.sql` → `init_mall_menus.sql`。

### 商品系统 PMS（menu 24~31, 28, 281）

| 菜单 | 中文 | 前端 component | 后端 API | 前端 |
|---|---|---|---|---|
| 25 | 分类维护 | `/mall/pms/category/index` | `/mall/category` | ✅ |
| 26 | 品牌列表 | `/mall/pms/brand/index` | `/mall/brand` | ✅ |
| 29~31 | 属性分组/规格/销售 | `/mall/pms/group/index` 等 | `/mall/group`、`/mall/attr` | ✅ |
| 180 | spu管理 | `/mall/pms/product/spu/index` | `/mall/product` | ✅（复用 SPU 列表） |
| 181 | 发布商品 | `/mall/pms/product/publish/index` | — | ✅（跳转 `/mall/product/add`） |
| 182 | 商品管理 | `/mall/pms/product/manager/index` | `/mall/product` | ✅（复用 SPU 列表） |
| 281 | SKU 管理 | `/mall/pms/sku/index` | `/mall/sku` | ✅ |

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

# 2. 商城业务库 + 菜单补充（perms/按钮 282~303、280 C端、281 SKU）
cd sql/gulimall
mysql -uroot -p < init_mall_all.sql

# 3. 已有库若跑过旧版 init_mall_menus（ID 冲突），先迁移再补充
mysql -uroot -p star_pivot < migrate_mall_menus_v2.sql
mysql -uroot -p star_pivot < init_mall_menus.sql
```

## 实体代码生成

```bash
cd starpivot-mall/scripts
node gen_entities.js
```

模块映射：`pms_*` → pms，`oms_*` / `mq_message` → oms，`sms_*` → sms，`ums_*` → ums，`wms_*` / `address` → wms。
