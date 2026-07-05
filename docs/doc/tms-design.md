# TMS 运输/物流管理（Phase 1 MVP）

## 能力概览

| 能力 | 说明 |
|------|------|
| 承运商主数据 | `tms_carrier`，含快递100 `com` 编码映射 |
| 运单管理 | `tms_shipment`，与商城订单 1:1 绑定 |
| 轨迹时间线 | `tms_track_event`，系统事件 + 快递100 拉取 |
| 发货闭环 | 管理端发货 → TMS 建单 → 回写 OMS → 消息通知 |
| C 端轨迹 | Portal `GET /portal/order/{id}/logistics` 内嵌时间线 |

## 模块与端口

| 项 | 值 |
|----|-----|
| 服务名 | `starpivot-tms` |
| 端口 | **9214** |
| 业务库 | `star_pivot_tms` |
| 网关路径 | `/api/v1/tms/**` |

## 部署

1. 执行 `sql/star_pivot_tms.sql`（表 + 承运商种子）
2. 执行 `sql/patch_tms_menu.sql`（菜单 323–330 + admin 角色绑定）
3. 导入 Nacos：`nacos/config/starpivot-tms.yaml`
4. 启动：`mvn spring-boot:run -pl starpivot-tms`
5. **重新登录**刷新菜单权限

## 发货流程

```
管理端「订单发货」→ POST /tms/shipment/ship
  → 校验待发货订单（Feign mall-order）
  → 创建 tms_shipment + 系统轨迹
  → PUT /internal/mall/order/deliver 回写 OMS
  → PUT /internal/ware/order-task/tracking 回写 WMS 工作单
  → 可选：快递100 拉取轨迹
  → 消息中心通知 admin 角色（MSG_TYPE_ORDER）
```

## Phase 2（已实现）

| 能力 | 说明 |
|------|------|
| WMS 联动 | 发货成功后回写 `wms_ware_order_task.tracking_no` |
| 轨迹定时刷新 | 开启快递100 后，每 5 分钟扫描在途运单（可配置） |
| WMS 界面 | 工作单列表/详情展示物流单号 |

配置项（Nacos `starpivot-tms.yaml`）：

```yaml
starpivot:
  tms:
    track-refresh-scan-ms: 300000   # 0 关闭定时刷新
    track-refresh-batch-size: 20
```

## Phase 3（已实现）

| 能力 | 说明 |
|------|------|
| 签收自动确认 | 快递100 识别 `DELIVERED` 后可选自动确认收货（默认关闭） |
| 运费规则 | `tms_freight_rule`，支持 FIXED / WEIGHT（按 SPU 重量） |
| 商城计价 | `mall-order` 通过 `TmsInternalClient.calculateFreight` 计算运费 |

配置项：

```yaml
starpivot:
  tms:
    auto-confirm-on-delivered: false   # true 启用签收自动确认
    default-item-weight-kg: 0.5      # SPU 无重量时的默认单件重量
  mall:
    order:
      use-tms-freight: true            # false 回退固定 defaultFreight
```

部署：执行 `sql/patch_tms_phase3.sql`（运费表 + 菜单 331–333）。

## Phase 4 规划

- 电子面单、一单多包裹
- 区域运费、承运商比价
- 快递100 签收 Webhook 推送

## 快递100 配置（可选）

在 Nacos `starpivot-tms.yaml` 或环境变量中启用：

```yaml
starpivot:
  tms:
    kuaidi100:
      enabled: true
      customer: ${KUAIDI100_CUSTOMER}
      key: ${KUAIDI100_KEY}
```

未配置时仍可发货；轨迹仅展示系统「已发货」事件，Portal 可 fallback 跳转快递100 公开查询页。

## 菜单与权限

| menu_id | 名称 | perms |
|---------|------|-------|
| 323 | 物流管理 | （目录） |
| 324 | 承运商配置 | `tms:carrier:query` / `tms:carrier:edit` |
| 325 | 运单管理 | `tms:shipment:query` / `tms:shipment:ship` / `tms:shipment:track` |

商城「订单发货」权限 `mall:order:deliver` 也可调用 TMS 发货接口。

## 管理端页面

- 承运商：`/tms/carrier`
- 运单：`/tms/shipment`
- 订单发货弹窗：承运商下拉 + 运单号（走 TMS）

## 已接入场景

| 场景 | 模块 | 说明 |
|------|------|------|
| 商城发货 | mall-order + tms | 发货经 TMS，OMS 字段冗余 |
| 物流查询 | portal | 内嵌轨迹 + 外链 fallback |
| 发货通知 | system 消息中心 | admin 角色 MSG_TYPE_ORDER |
| WMS 运单回写 | mall-ware | 发货后同步 tracking_no |

## Phase 2 规划

- ~~WMS 出库任务 ↔ 运单自动联动~~ ✅
- 电子面单、一单多包裹
- 运费规则（PMS 重量）
- 签收 Webhook → 自动确认收货
