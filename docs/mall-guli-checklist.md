# 商城与谷粒商城对照清单

> 对照尚硅谷谷粒商城（Guli Mall）典型课程结构，评估 StarPivot 商城实现完整度。  
> 微服务启动见 [mall-startup.md](./mall-startup.md)，菜单与 API 见 [mall.md](./mall.md)。

**图例**：✅ 已实现 · ⚠️ 部分实现或实现方式不同 · ❌ 未做 · ➕ StarPivot 增强（谷粒原版无）

**总体**：核心业务约 **85% 对齐**；课程后半分布式专题（Seata、MQ 关单、Sentinel、Canal）约 **30% 对齐**。

---

## 0. 基础架构

| 章节 | 谷粒 | StarPivot | 状态 |
|------|------|-----------|------|
| 微服务拆分 | product / ware / order / member / coupon + search + third-party | member / product / ware / order / promotion + app(BFF) | ✅ |
| 注册中心 Nacos | ✅ | ✅ | ✅ |
| 配置中心 Nacos | ✅ | ✅ `nacos/config/starpivot-mall-*.yaml` | ✅ |
| API 网关 | gulimall-gateway | starpivot-gateway | ✅ |
| 认证中心 | gulimall-auth-server | starpivot-auth + member 内 C 端 JWT | ⚠️ |
| 公共模块 | gulimall-common | starpivot-mall-common + starpivot-api(Feign) | ✅ |
| 数据库 | 各服务独立库 | 五域分库（product/ware/order/member/promotion） | ✅ 与 Nacos 配置一致 |

### 服务端口对照

| 谷粒服务 | StarPivot 服务 | 端口 |
|----------|----------------|------|
| gulimall-member | starpivot-mall-member | 9206 |
| gulimall-product | starpivot-mall-product | 9207 |
| gulimall-ware | starpivot-mall-ware | 9208 |
| gulimall-order | starpivot-mall-order | 9209 |
| gulimall-coupon | starpivot-mall-promotion | 9210 |
| gulimall-search | （合并在 product） | — |
| gulimall-third-party | starpivot-file + 各服务 OSS 配置 | 9202 |
| — | starpivot-mall（静态资源 BFF） | 9205 |

---

## 1. 商品服务（gulimall-product）

| 功能 | 状态 | 所在服务 |
|------|------|----------|
| 三级分类 CRUD | ✅ | product |
| 品牌管理 + 分类关联 | ✅ | product |
| 属性分组 / 规格 / 销售属性 | ✅ | product |
| SPU 发布（7 表级联） | ✅ | product |
| SKU 笛卡尔积生成 | ✅ | 前端 `spu-descartes.ts` + 后端 |
| SKU 管理 / 商品管理 | ✅ | product |
| 商品评论 B 端 | ✅ | product |
| 商品图片上传 | ✅ | product（OSS / 本地） |
| C 端商品搜索 / 详情 | ✅ | product |
| ES 商品索引 | ⚠️ | 合并在 product，`MALL_ES_ENABLED=false` 默认走 DB |
| Canal 监听 binlog 同步 ES | ❌ | 改为 SPU 变更时主动 sync |
| 独立 gulimall-search 服务 | ❌ | 能力有，结构不同 |

---

## 2. 仓储服务（gulimall-ware）

| 功能 | 状态 | 所在服务 |
|------|------|----------|
| 仓库维护 | ✅ | ware |
| 商品库存 wms_ware_sku | ✅ | ware |
| 省市区三级联动 | ✅ | ware（`address` 表） |
| 采购单 / 采购明细 | ✅ | ware |
| 库存工作单（锁库/扣库） | ✅ | ware |
| 采购需求独立表 | ❌ | `wms_purchase_demand` 未建 |
| C 端 `/portal/region` | ✅ | ware |

---

## 3. 会员服务（gulimall-member）

| 功能 | 状态 | 所在服务 |
|------|------|----------|
| 注册 / 密码登录 | ✅ | member |
| 收货地址 CRUD | ✅ | member |
| 会员等级 / 积分 / 成长值 | ✅ | member |
| 会员统计 | ✅ | member |
| 社交登录（微博/微信字段） | ⚠️ | 表字段保留；实际用短信+微信 OAuth |
| 短信验证码登录 | ➕ | member |
| 微信登录 / 绑定 | ➕ | member |
| C 端收藏 | ✅ | member |
| B 端登录日志 | ✅ | member |
| B 端收藏管理 | ✅ | member |
| 下单后积分/成长值回写 | ✅ | member ← order Feign |

---

## 4. 营销服务（gulimall-coupon）

| 功能 | 状态 | 所在服务 |
|------|------|----------|
| 优惠券 CRUD + 发放记录 | ✅ | promotion |
| 专题活动 | ✅ | promotion |
| 秒杀活动 / 场次 / SKU | ✅ | promotion |
| 满减 / 阶梯价 / 会员价 | ✅ | promotion |
| 积分赠送 sms_spu_bounds | ✅ | promotion |
| 首页轮播 | ✅ | promotion |
| 分类热门 | ✅ | promotion |
| C 端首页 / 优惠券 / 秒杀 | ✅ | promotion |
| 秒杀 Redis + Lua 脚本 | ✅ | promotion |
| 优惠券审批流 | ➕ | promotion + approval |

---

## 5. 订单服务（gulimall-order）

| 功能 | 状态 | 所在服务 |
|------|------|----------|
| Redis 购物车 | ✅ | order |
| 结算页 / 价格试算 | ✅ | order + promotion Feign |
| 下单防重 token | ✅ | order |
| 库存预扣 / 释放 | ✅ | order + ware Feign |
| Mock 支付 | ✅ | order |
| 支付宝沙箱 | ✅ | order |
| 微信 Native 支付 | ✅ | order |
| 订单查询 / 发货 / 关单 | ✅ | order |
| 退货申请 / 审核 | ✅ | order |
| 支付 / 退款流水 | ✅ | order |
| 订单设置（超时规则） | ✅ | order |
| 自动确认收货 / 评价期结束 | ✅ | order 定时任务 |
| 超时关单 | ⚠️ | 定时扫描，非 RabbitMQ 死信队列 |
| RabbitMQ 订单延迟关单 | ❌ | 谷粒经典方案未采用 |
| 支付宝原路退款 | ⚠️ | 部分待完善 |

---

## 6. 检索服务（gulimall-search）

| 功能 | 状态 |
|------|------|
| ES 索引 / 搜索 / 聚合 | ⚠️ 合并在 product，可选启用 |
| 上架过滤 / 品牌筛选 | ✅ |
| 独立 search 微服务 | ❌ |

启用 ES：在 Nacos `starpivot-mall-product.yaml` 或环境变量设置 `MALL_ES_ENABLED=true`。

---

## 7. 第三方服务（gulimall-third-party）

| 功能 | 状态 |
|------|------|
| OSS 对象存储 | ⚠️ 公共 `oss-config.yaml` + starpivot-file / product |
| 短信 | ⚠️ member 内短信登录 |
| 本地静态资源 | ✅ mall-app `/local-storage/**` |
| 统一文件中心 objectKey | ❌ 待规范 |

---

## 8. 分布式与高可用（课程后半）

| 功能 | 谷粒 | StarPivot |
|------|------|-----------|
| Seata AT 分布式事务 | ✅ 课程重点 | ❌ 规划 P3 |
| mq_message 可靠消息 | ✅ | ❌ 表有，业务未接 |
| Sentinel 限流熔断 | ✅ | ❌ |
| RabbitMQ 业务消息 | ✅ 关单/解锁 | ⚠️ 仅审批回调 MQ |
| 接口幂等 / 防重复提交 | ✅ | ✅ orderToken |

---

## 9. 前端

| 模块 | 状态 |
|------|------|
| B 端 PMS/WMS/OMS/SMS/UMS/CMS | ✅ `star-pivot-ui/views/mall/` |
| C 端 Portal | ✅ `views/portal/` |
| 微信小程序 | ➕ `star-pivot-mp/` |

---

## mall-app 清理说明

`starpivot-mall-app`（artifactId: `starpivot-mall`）**仅保留静态资源 BFF**，网关只路由 `/local-storage/**` 到此服务。

### 保留文件

| 文件 | 作用 |
|------|------|
| `StarPivotMallApplication.java` | 启动类 |
| `config/LocalStorageResourceConfig.java` | 映射 `/local-storage/**` |
| `config/MallAppSecurityConfig.java` | 仅放行静态资源 |
| `application.yml` | Nacos + 本地存储路径 |

### 原 monolith Controller 迁移去向

| 模块 | 原 mall-app Controller | 迁移至 |
|------|------------------------|--------|
| PMS | Category/Brand/Attr/Spu/Sku/Comment/ProductSearch/Image | product |
| WMS | WareInfo/WareSku/Purchase/WareOrderTask/Address | ware |
| OMS | Order/Return/Setting/Payment/Refund | order |
| SMS | Coupon/Seckill/HomeAdv/Subject/满减/阶梯/会员价 | promotion |
| Portal | Home/Subject/Coupon/Seckill | promotion |
| Portal | Product/Image/Comment | product |
| Portal | Cart/Order/MockPay/Alipay/WxPay | order |
| Portal | Region | ware |
| Internal | MallOrderInternalController | order |

拆分服务新增（mall-app 从未有）：`MemberInternalController`、`ProductInternalController`、`WareInternalController`、`PromotionInternalController`、`MallStockInternalController`、`PortalMemberAuthController`、`UmsMemberCollectController`、`UmsMemberLoginLogController` 等。

---

## 待办优先级（对齐谷粒课程完整度）

| 优先级 | 项 | 说明 |
|--------|-----|------|
| P1 | mall-app 清理已提交 | 消除 Git 中遗留业务代码 |
| P2 | 启用 ES | `MALL_ES_ENABLED=true` + 重建索引 |
| P2 | 省市区数据 | 执行 `sql/gulimall/gulimall_address_data.sql` |
| P3 | Seata 或 mq_message | 跨服务强一致 |
| P3 | RabbitMQ 延迟关单 | 替代/补充定时扫描 |
| P3 | Sentinel | 秒杀/下单限流 |
| P4 | 支付宝原路退款 | 退货闭环 |

---

## 相关文档

- [mall-startup.md](./mall-startup.md) — 微服务启动与 Nacos 配置
- [mall.md](./mall.md) — 菜单与 API 对照
- [商城开发事项.md](./商城开发事项.md) — 表设计与开发进度
