# 商城分布式事务与可靠消息

> P3 增强：Seata AT 与 `mq_message` 本地消息表（Outbox）。

## 现状

| 场景 | 当前方案 | 说明 |
|---|---|---|
| 单服务内支付确认 | `@Transactional` | order 域内 pay → payment 流水原子 |
| 支付后扣 WMS 库存 | **Outbox（可选）** 或同步 Feign | 开关 `order-paid-outbox-enabled` |
| 待付款超时关单 | **延迟 MQ（可选）** 或 Redis ZSET 扫描 | 开关 `order-close-delay-mq-enabled` |
| 支付后积分/优惠券/销量 | **跟进 Outbox（可选）** 或同步 Feign | 开关 `order-paid-followup-outbox-enabled`；销量仍同步 |
| 审批完结回写 | RabbitMQ + 幂等消费 | 已用于退货/采购/优惠券等 |
| 跨服务强一致 | **未引入 Seata** | 失败依赖补偿或人工 |

## Seata AT（暂缓）

**不引入原因**（与审批中台设计一致）：

- 运维成本高（Seata Server、undo_log、全局锁）
- 支付回调 + 多下游更适合最终一致
- Outbox + MQ 已覆盖 ware 扣库存场景

**若后续需要**：在支付确认入口加 `@GlobalTransactional`，并部署 Seata TC；`oms_order` 库已有 `undo_log` 表占位。

## mq_message 本地消息表（Outbox）

### 表结构

`star_pivot_order.mq_message`：`message_id` · `content` · `to_exchange` · `routing_key` · `class_type` · `message_status`

| status | 含义 |
|---|---|
| 0 | 新建，待投递 |
| 1 | 已发送至交换机 |
| 2 | 投递失败 |
| 3 | 消费者已确认（预留） |

### 已实现组件

| 类 | 职责 |
|---|---|
| `OmsLocalMessageOutboxService` | 业务事务内 `enqueue()` / `flushMessage()` |
| `PortalOrderPaidOutboxPublisher` | 支付成功登记 `OrderPaidMessage` |
| `MallWareOrderPaidMqListener` | ware 消费 `mall.order.paid` |
| `WmsOrderPaidFulfillmentService` | 扣 WMS 库存 + 生成已完成工作单 |

### 启用

```yaml
starpivot:
  mq:
    enabled: true
  mall:
    order-paid-outbox-enabled: true
```

## 待付款超时关单（延迟 MQ）✅ Phase 3

### 机制

谷粒经典 **TTL 延迟队列 + DLX**（无需 RabbitMQ 延迟插件）：

```
下单成功
  → 投递 starpivot.mall.order-close.delay（per-message expiration = 订单超时分钟）
  → TTL 到期 → DLX → routingKey mall.order.close
  → starpivot.mall.order-close 队列
  → MallOrderCloseMqListener → PortalOrderCloseService
```

### 已实现组件

| 类 | 职责 |
|---|---|
| `PortalOrderCloseDelayPublisher` | 下单后投递延迟关单消息 |
| `MallOrderCloseMqListener` | 消费 `OrderCloseMessage` |
| `PortalOrderCloseService` | 释放 Redis 预扣 + 关单 + 回滚优惠券/积分 |

开启延迟 MQ 后：

- 不再写入 Redis `STOCK_LOCK_EXPIRY_ZSET`
- `PortalStockLockScheduler` 自动停用（仍可用 Redis 路径作为 fallback）

### 启用

```yaml
starpivot:
  mq:
    enabled: true
  mall:
    order-close-delay-mq-enabled: true   # MALL_ORDER_CLOSE_DELAY_MQ_ENABLED=true
```

超时分钟数读取 `oms_order_setting.normal_order_overtime`（与库存锁 TTL 一致）。

### 幂等

- 消费幂等键：`mall:order:close:{orderId}`
- 已支付（`stockLockConfirmedKey` 存在）或订单非待付款时直接跳过

## 支付后优惠券/积分跟进（Outbox）✅ Phase 4

### 机制

支付成功事务内写入两条 `mq_message`，分别路由至 promotion / member：

```
支付成功（order 事务）
  → Outbox: mall.order.paid.coupon  → MallPromotionOrderPaidMqListener → confirmUsed
  → Outbox: mall.order.paid.reward  → MallMemberOrderPaidMqListener → grantOnPaid
```

与 Phase 2 ware 扣库存 **独立开关**，可单独或组合启用。

### 已实现组件

| 类 | 职责 |
|---|---|
| `PortalOrderPaidFollowupOutboxPublisher` | 登记 `OrderPaidFollowupMessage`（coupon + reward） |
| `MallPromotionOrderPaidMqListener` | promotion 消费优惠券确认 |
| `MallMemberOrderPaidMqListener` | member 消费积分/成长值发放 |

### 启用

```yaml
starpivot:
  mq:
    enabled: true
  mall:
    order-paid-followup-outbox-enabled: true   # MALL_ORDER_PAID_FOLLOWUP_OUTBOX_ENABLED=true
```

### 幂等

- 优惠券：`mall:order:paid:coupon:{orderId}`；`confirmUsed` 对无锁定券直接跳过
- 会员奖励：`mall:order:paid:reward:{orderId}`；`grantOnPaid` 按订单号查历史防重

## 推荐演进路径

1. **Phase 1** — Outbox 基础设施 ✅
2. **Phase 2** — 支付成功 → ware 异步扣库存 ✅
3. **Phase 3** — 超时关单延迟 MQ ✅
4. **Phase 4** — 优惠券/积分回写 Outbox 化 ✅
5. **Phase 5** — 评估 Seata

## 相关文档

- [mq-usage.md](./doc/mq-usage.md) — RabbitMQ 接入与监听器模板
- [商城开发事项.md](./商城开发事项.md) — 总体进度
