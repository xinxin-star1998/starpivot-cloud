# Java 微服务版本审批中心设计方案（starpivot-cloud 适配版）

> **文档说明**：本文档为 `starpivot-cloud` 项目专用审批中心方案，在通用微服务审批架构基础上完成技术栈与架构收敛。
>
> **引擎选型**：SAS（StarPivot Approval Service）自研阶梯流水线引擎，**不采用 Flowable**。
>
> **详细开发规格**：表结构 DDL、引擎伪代码、MQ 契约、前端目录见 [`docs/doc/workflow-design.md`](doc/workflow-design.md)。

---

# 一、方案概述

## 1.1 项目背景

`starpivot-cloud` 采用 Spring Cloud Alibaba 微服务架构，当前包含 gateway、auth、system、file、mall、mq 等模块。商城业务（采购、退货、优惠券、商品上架等）及后续 OA 场景均需要审批能力。

**现状问题**：

| 问题 | 说明 |
|------|------|
| 前端有壳、后端无服务 | `views/workflow/*` 从单体拷贝，cloud 内无 workflow Java 代码，菜单 154~168 不可用 |
| 审批逻辑分散 | 如退货在 OMS 内联 `audit()`，采购无审批环节，优惠券直接改 publish 状态 |
| SPF 图引擎不适配微服务 | StarPivot 单体的 Vue Flow + `defJson` 方案依赖同进程 Listener，无法跨服务解耦 |

因此需要建设**统一审批中台**，为 mall 及后续业务提供标准化、可复用、弱耦合的审批服务。

## 1.2 设计目标

- **微服务解耦**：独立部署 `starpivot-approval`，业务通过 Feign 提交、MQ 接收完结事件
- **流程可配置**：模板 + 步骤表 + SpEL 条件路由，无需改代码即可适配新场景
- **轻量可维护**：阶梯流水线引擎，避免 BPMN / Flowable 过重引入
- **权限精细化**：复用 `starpivot-system` 组织数据，支持角色、岗位、部门负责人、或签/会签
- **全流程追溯**：`ap_record` 只增不改，步骤表可直接 SQL 审计
- **易集成对接**：标准化 REST + RabbitMQ 事件，参照 `starpivot-file` 中台模式

## 1.3 适用场景

**首期（商城 B 端）**：

| bizType | 说明 | 审批链示例 |
|---------|------|------------|
| `purchase` | 采购单 | 部门负责人 → 财务（amount>1 万加总监） |
| `return` | 退货申请 | 客服主管 → 财务 |
| `coupon` | 优惠券发布 | 运营主管 → 财务 |
| `spu` | 商品上架 | 品类负责人 → 质控 |

**后续扩展**：人事、财务、合同等 OA 场景，沿用同一套 `bizModule + bizType + template_bind` 机制接入。

## 1.4 与通用方案 / 旧方案的对照

| 维度 | 通用 OA 方案（Flowable + 六服务） | SPF 图引擎（单体，弃用） | **本方案 SAS** |
|------|----------------------------------|-------------------------|----------------|
| 流程引擎 | Flowable BPMN | 自研图遍历 + 编译器 | PipelineEngine 阶梯流水线 |
| 服务形态 | 6 个微服务 | 单体模块 | **1 个** `starpivot-approval` |
| 消息队列 | RocketMQ | 同进程 Listener | **RabbitMQ**（`starpivot-mq`） |
| Spring 版本 | Boot 2.7 | Boot 3 | **Boot 3.3**（与项目一致） |
| 表单 | 动态表单引擎 | 业务自带 | **业务页即表单**，context 快照 |
| 前端设计器 | Flowable Modeler | Vue Flow 画布 | **步骤配置表 + 时间轴** |

---

# 二、总体架构设计

## 2.1 架构模式

采用 **Spring Cloud Alibaba 微服务架构**，审批能力封装为独立中台服务，与业务服务通过 **Feign（同步）+ RabbitMQ（异步）** 协作。参照已落地的 `starpivot-file` 文件中心模式。

## 2.2 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│ star-pivot-ui（管理端）                                          │
│  /approval/template  /approval/todo  /approval/mine              │
│  业务详情页嵌入 ApprovalTimeline                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ starpivot-gateway                                                │
│  Path=/api/v1/approval/**  →  lb://starpivot-approval           │
└────────────────────────────┬────────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ starpivot-mall-*│  │ starpivot-system│  │starpivot-approval│
│ 提交 / 消费 MQ   │  │ 用户/角色/部门   │  │ PipelineEngine  │
└────────┬────────┘  └────────▲────────┘  └────────┬────────┘
         │                      │ Feign              │
         │    Feign submit      │                    │ MySQL ap_*
         └──────────────────────┼────────────────────┘
                                │
         ┌──────────────────────┴──────────────────────┐
         │ RabbitMQ（starpivot-mq）                     │
         │ routing: approval.instance.finished          │
         └─────────────────────────────────────────────┘
```

## 2.3 分层说明

### 2.3.1 接入层

- **管理后台**：`star-pivot-ui`，路由前缀 `/approval/*`
- **业务页面**：采购单、退货等详情页嵌入 `ApprovalTimeline` 组件
- **服务间调用**：业务微服务通过 `starpivot-api` 中的 `ApprovalInternalClient`（服务间提交/补偿）与 `ApprovalClient`（用户侧查时间轴等）

### 2.3.2 网关层

基于现有 `starpivot-gateway`，新增路由（待实施）：

```yaml
- id: starpivot-approval
  uri: lb://starpivot-approval
  predicates:
    - Path=/api/${starpivot.api.version}/approval/**
```

统一完成 JWT 鉴权、路由转发、跨域处理（与现有 gateway 白名单策略一致）。

### 2.3.3 核心服务层

**首期仅部署一个微服务 `starpivot-approval`**，内部按包划分职责：

| 包 / 组件 | 职责 |
|-----------|------|
| `ApTemplateController` | 模板 CRUD、发布、停用、业务绑定 |
| `ApInstanceController` | 提交、撤回、我发起、时间轴 |
| `ApTaskController` | 待办、已办、通过、驳回 |
| `PipelineEngine` | 阶梯推进、状态机 |
| `AssigneeResolver` | 解析审批人（Feign 调 system） |
| `RouteEvaluator` / `SkipEvaluator` | SpEL 条件路由与步骤跳过 |
| `ApprovalFinishedPublisher` | 完结事件发布 |

**复用现有基础服务（不重复建设）**：

| 服务 | 用途 |
|------|------|
| `starpivot-system` | 用户、角色、岗位、部门负责人 |
| `starpivot-file` | 审批附件（Category=`OA`，前缀 `file/oa/`） |
| `starpivot-auth` | JWT 鉴权 |
| `starpivot-mq` | 消息发布 / 幂等消费 |

> **说明**：消息推送、统计看板在 Phase 3 按需扩展；首期不拆独立 message / stat 微服务。

### 2.3.4 数据持久层

- **MySQL**：`starpivot` 主库，`ap_*` 七张表（与 system 同库部署；组织数据经 Feign 调 system，不本地复制）
- **Redis**：可选缓存热点模板；待办数量缓存 Phase 3
- **ORM**：MyBatis-Plus（与项目一致）

### 2.3.5 基础设施层

| 组件 | 项目现状 | 审批中心用法 |
|------|----------|--------------|
| Nacos | ✅ 已用 | 服务注册、动态配置 |
| RabbitMQ | ✅ `starpivot-mq` | 审批完结事件 |
| Redis | ✅ 已用 | 防重、可选缓存 |
| Sentinel | 可选 | 网关 / 服务限流 |
| Seata | P3 待完善 | **首期不需要**；完结走 MQ 最终一致 |

---

# 三、核心技术选型

| 技术模块 | 技术选型 | 用途说明 |
|----------|----------|----------|
| 开发框架 | Spring Boot 3.3 + Spring Cloud Alibaba | 与 `starpivot-cloud` 一致 |
| 流程引擎 | **SAS PipelineEngine（自研）** | 阶梯审批 + SpEL 路由，非 Flowable |
| 网关 | Spring Cloud Gateway | 已有，新增 approval 路由 |
| 注册 & 配置 | Nacos | 已有 |
| 服务调用 | OpenFeign | `ApprovalInternalClient`（服务间）、`ApprovalClient`（时间轴）、组织数据查询 |
| 消息队列 | **RabbitMQ** | 异步完结通知（`starpivot-mq`） |
| 数据库 | MySQL 8.0 | `ap_*` 表 |
| 缓存 | Redis | 防重复提交、可选模板缓存 |
| ORM | MyBatis-Plus | 与项目一致 |
| 条件表达式 | Spring SpEL | 路由、跳过、模板绑定匹配 |
| 权限 | menu-permission + JWT | 与 system / mall 一致 |
| 链路追踪 | 可选 SkyWalking | 与运维规划对齐 |

---

# 四、核心业务设计

## 4.1 流程引擎核心设计（SAS PipelineEngine）

摒弃 BPMN 与硬编码流程，采用**有序审批阶梯 + 可选条件路由**。

### 4.1.1 流程模板（ap_template + ap_template_step）

| 配置项 | 说明 |
|--------|------|
| 模板基础信息 | `template_code`、`template_name`、`biz_module`、版本、状态（DRAFT/PUBLISHED/DISABLED） |
| 步骤配置 | 有序步骤：步骤名、审批人策略、或签/会签、跳过表达式 |
| 条件路由 | `ap_template_route`：某步完成后按 SpEL 决定下一步 |
| 业务绑定 | `ap_template_bind`：按 `bizModule + bizType + match_expr` 自动匹配模板 |

**默认流转形态**：

```
提交 → [步骤1：部门负责人] → [步骤2：财务] → [步骤3：总监] → 通过
                ↓ 驳回（任意步骤）
              整单结束（REJECTED）
```

### 4.1.2 流程实例（ap_instance）

用户 / 业务服务发起审批后生成唯一实例，核心字段：

- `biz_key`：全局业务键，如 `mall:purchase:10001`
- `starter_id`、`status`、`current_step_id`
- `context_json`：审批上下文（金额、供应商 ID 等），供 SpEL 与展示

**约束**：同一 `biz_key` 仅允许一个 `RUNNING` 实例。

### 4.1.3 核心流转逻辑

1. 业务服务调用 `ApprovalInternalClient.submit()`（内部接口，携带 `starterId`），传入 `bizModule、bizType、bizKey、title、context`
2. 引擎按 `ap_template_bind` 匹配已发布模板，创建实例与 SUBMIT 记录
3. `enterStep`：解析审批人 → 生成 `ap_task`（PENDING）；或签/会签按步骤配置
4. 审批人通过 → `advanceFrom` 评估路由或线性下一步（实例行 `FOR UPDATE` 防并发）；驳回 → 实例 REJECTED，取消全部待办
5. 最后一步通过 → 实例 APPROVED，**事务提交后**发布 MQ `ApprovalFinishedMessage`
6. 业务服务消费 MQ，在 `audit_status=PENDING` 时更新状态并触发后续业务（入库、退款等）

### 4.1.4 审批人策略（assigneeType）

| 类型 | 说明 |
|------|------|
| `STARTER` | 发起人本人 |
| `DEPT_LEADER` | 发起人部门负责人（`sys_dept.leader`） |
| `ROLE` | 角色 `roleKey` 下启用用户 |
| `POST` | 岗位 `postCode` 下启用用户 |
| `USER` | 指定用户 ID |
| `DEPT_ROLE` | 指定部门 + 角色（扩展） |

解析失败时**提交/推进失败并返回明确错误**，不静默跳过。

### 4.1.5 首期不支持的能力

驳回到上一节点、加签、转办、抄送、子流程、并行网关——留 Phase 3 扩展。

## 4.2 审批状态设计

### 4.2.1 实例状态（ap_instance.status）

| 状态 | 含义 | 对应通用方案 |
|------|------|--------------|
| `RUNNING` | 审批进行中 | PENDING |
| `APPROVED` | 审批通过 | AGREE |
| `REJECTED` | 审批驳回 | REJECT |
| `WITHDRAWN` | 发起人撤回 | CANCEL |

### 4.2.2 业务表 audit_status（各业务服务）

| 状态 | 含义 |
|------|------|
| `DRAFT` | 草稿，未提交 |
| `PENDING` | 已提交，审批中 |
| `APPROVED` | 已通过 |
| `REJECTED` | 已驳回 |
| `WITHDRAWN` | 已撤回 |

业务表建议增加字段：`approval_instance_id`、`audit_status`。

## 4.3 权限规则设计

复用 `starpivot-system` RBAC，审批中台独立权限前缀 `approval:*`：

| 权限 | 说明 |
|------|------|
| `approval:template:query` | 查模板 |
| `approval:template:edit` | 编辑模板/步骤 |
| `approval:template:publish` | 发布 |
| `approval:bind:edit` | 业务绑定 |
| `approval:task:query` | 待办/已办 |
| `approval:task:action` | 通过/驳回 |
| `approval:instance:query` | 我发起、时间轴 |
| `approval:instance:withdraw` | 撤回 |
| `approval:instance:submit` | 管理端/通用提交（**业务服务提交走内部接口，见 §6.4**） |

节点审批人由模板步骤 + `AssigneeResolver` 决定，不单独建设 approval-perm 微服务。

**业务服务提交权限**：mall 等微服务通过 Feign 直连 `/internal/approval/**`，由 `X-Internal-Token` 鉴权，**不要求**操作用户具备 `approval:instance:submit`。发起人身份由请求体 `starterId` 传入（取自当前登录用户）。

## 4.4 消息通知设计

基于 **RabbitMQ**（`starpivot-mq`）异步解耦，**不阻塞审批主流程**。

### 4.4.1 完结事件（主集成方式）

| 项 | 值 |
|----|-----|
| Routing Key（商城） | `approval.instance.finished.mall.{bizType}`（purchase / spu / return / coupon） |
| 消费队列 | `starpivot.mall.approval-finished.purchase` → ware；`...spu` → product；`...return` → order；`...coupon` → promotion |
| 消息体 | `ApprovalFinishedMessage`（instanceId、bizModule、bizType、bizKey、result、starterId、finishTime） |
| result | `APPROVED` / `REJECTED` / `WITHDRAWN` |

**原则**：Feign 用于 submit、timeline 等同步操作；**完结通知走 MQ**，审批服务不反向依赖业务服务。

**发送时机**：`ApprovalFinishedPublisher` 在 **数据库事务提交后**（`TransactionSynchronization.afterCommit`）再调用 `MqPublisher`，避免事务回滚后出现幽灵消息。MQ 发送失败仅记日志，不回滚审批主事务。

**系统补偿撤回**：服务间调用 `POST /internal/approval/instance/{id}/withdraw` 时 **不发布** 完结 MQ（避免商城侧误更新 `audit_status`）。

### 4.4.2 站内信 / 钉钉提醒（Phase 3）

待办提醒、超时提醒、结果通知——复用 system 站内信或对接 IM，首期 MVP 可仅依赖待办列表页。

---

# 五、数据库核心表设计

脚本路径：`sql/init_approval.sql`。完整 DDL 见 [`docs/doc/workflow-design.md`](doc/workflow-design.md) §5。

## 5.1 表清单

| 表名 | 说明 | 对应通用方案 |
|------|------|--------------|
| `ap_template` | 审批模板主表 | approval_template |
| `ap_template_step` | 模板步骤（有序） | —（Flowable 存 BPMN） |
| `ap_template_route` | 步骤完成后条件路由 | — |
| `ap_template_bind` | 业务类型与模板绑定 | — |
| `ap_instance` | 审批实例 | approval_instance |
| `ap_task` | 待办任务 | approval_task |
| `ap_record` | 审批记录（只 INSERT） | approval_log |

## 5.2 核心字段摘要

### ap_template

| 字段 | 类型 | 说明 |
|------|------|------|
| template_id | bigint | 主键 |
| template_code | varchar(64) | 模板编码，如 `mall_purchase_default` |
| template_name | varchar(128) | 模板名称 |
| biz_module | varchar(32) | 业务域：mall / system |
| version | int | 版本号 |
| status | varchar(16) | DRAFT / PUBLISHED / DISABLED |

### ap_instance

| 字段 | 类型 | 说明 |
|------|------|------|
| instance_id | bigint | 主键 |
| biz_module / biz_type | varchar | 业务域与单据类型 |
| biz_key | varchar(128) | 如 `mall:purchase:10001` |
| title | varchar(256) | 审批标题 |
| starter_id | bigint | 发起人 |
| status | varchar(16) | RUNNING / APPROVED / REJECTED / WITHDRAWN |
| context_json | json | 审批上下文 |

### ap_task

| 字段 | 类型 | 说明 |
|------|------|------|
| task_id | bigint | 主键 |
| instance_id | bigint | 关联实例 |
| step_code / step_name | varchar | 当前步骤 |
| assignee_id | bigint | 审批人 |
| status | varchar(16) | PENDING / DONE / CANCELLED |
| action / comment | varchar | 审批动作与意见 |

---

# 六、核心接口设计

API 前缀：`/api/v1/approval`（经网关对外暴露）。

## 6.1 流程模板接口

| 功能 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 模板分页 | POST | `/template/templatePageList` | `approval:template:query` |
| 模板详情（含步骤） | GET | `/template/{id}` | `approval:template:query` |
| 保存模板+步骤 | POST | `/template/save` | `approval:template:edit` |
| 发布 | POST | `/template/{id}/publish` | `approval:template:publish` |
| 停用 | POST | `/template/{id}/disable` | `approval:template:edit` |
| 绑定规则分页 | POST | `/template/templateBindPageList` | `approval:bind:edit` |
| 保存绑定规则 | POST | `/template/bind/save` | `approval:bind:edit` |
| 删除绑定规则 | DELETE | `/template/removeTemplateBind` | `approval:bind:edit` |

> 对应通用方案 `POST /template/deploy` → 本方案 **publish**（无 Flowable 部署步骤）。

## 6.2 审批实例接口

| 功能 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 提交审批 | POST | `/instance/submit` | `approval:instance:submit` |
| 撤回 | POST | `/instance/{id}/withdraw` | `approval:instance:withdraw` |
| 我发起的 | POST | `/instance/mineInstancePageList` | `approval:instance:query` |
| 审批时间轴 | GET | `/instance/{id}/timeline` | `approval:instance:query` |

## 6.2.1 内部接口（Feign 直连，不经网关）

路径前缀 `/api/v1/internal/approval`，由 `InternalServiceAuthFilter` 校验 `X-Internal-Token`（配置项 `starpivot.internal.token`）。详见 [`docs/security/permission-strategy.md`](security/permission-strategy.md)。

| 功能 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 服务间提交 | POST | `/internal/approval/instance/submit` | 请求体为 `InternalApprovalSubmitRequest`（含 `starterId`） |
| 系统补偿撤回 | POST | `/internal/approval/instance/{id}/withdraw` | 本地落库失败时撤回孤儿实例，不发 MQ |

**内部提交请求体示例**：

```json
{
  "bizModule": "mall",
  "bizType": "purchase",
  "bizKey": "mall:purchase:10001",
  "title": "采购单审批 #10001",
  "starterId": 100,
  "templateCode": null,
  "context": { "amount": 12800 }
}
```

**提交请求体示例**：

```json
{
  "bizModule": "mall",
  "bizType": "purchase",
  "bizKey": "mall:purchase:10001",
  "title": "采购单审批 #10001",
  "templateCode": null,
  "context": {
    "amount": 12800,
    "supplierId": 5
  }
}
```

## 6.3 审批任务接口

| 功能 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 待办列表 | POST | `/task/todoTaskPageList` | `approval:task:query` |
| 已办列表 | POST | `/task/doneTaskPageList` | `approval:task:query` |
| 通过 | POST | `/task/approve` | `approval:task:action` |
| 驳回 | POST | `/task/reject` | `approval:task:action` |

## 6.4 业务服务对接（Feign + MQ）

**同步 — 服务间提交（推荐，starpivot-api）**：

```java
@FeignClient(name = "starpivot-approval", contextId = "approvalInternalClient")
public interface ApprovalInternalClient {
    @PostMapping("/internal/approval/instance/submit")
    Result<Long> submit(@RequestBody InternalApprovalSubmitRequest request);

    @PostMapping("/internal/approval/instance/{id}/withdraw")
    Result<Void> withdraw(@PathVariable("id") Long instanceId);
}
```

`InternalApprovalSubmitRequest` 继承 `ApprovalSubmitRequest`，额外携带 `starterId`（当前登录用户 ID）。Feign 调用 `/internal/**` 时由 `InternalFeignRequestInterceptor` 自动附加 `X-Internal-Token`。

**同步 — 用户侧查询时间轴**：

```java
@FeignClient(name = "starpivot-approval", contextId = "approvalClient")
public interface ApprovalClient {
    @GetMapping("/approval/instance/{id}/timeline")
    Result<ApprovalTimelineVo> timeline(@PathVariable("id") Long id);
}
```

**提交与补偿（mall 参考实现）**：

```java
Long instanceId = null;
try {
    InternalApprovalSubmitRequest request = buildRequest(...);
    request.setStarterId(SecurityContextUtils.getUserId());
    instanceId = approvalInternalClient.submit(request).getData();
    // 本地更新 approval_instance_id、audit_status=PENDING
} catch (RuntimeException ex) {
    if (instanceId != null) {
        approvalInternalClient.withdraw(instanceId);  // 补偿撤回孤儿实例
    }
    // 回滚 audit_status=DRAFT
    throw ex;
}
```

**异步（mall 消费示例）**：

```java
@Component
public class MallApprovalFinishedMqListener extends AbstractMqListener<ApprovalFinishedMessage> {

    @Override
    protected String resolveIdempotentKey(Message message, MessageEnvelope<ApprovalFinishedMessage> envelope) {
        ApprovalFinishedMessage payload = envelope.getPayload();
        return "approval:finished:" + payload.getInstanceId() + ":" + payload.getResult();
    }

    private void dispatch(ApprovalFinishedMessage msg) {
        // 各 Service 按 bizModule/bizType 过滤；仅 audit_status=PENDING 时更新
        purchaseApprovalService.handleApprovalFinished(...);
        returnApprovalService.handleApprovalFinished(...);
    }
}
```

## 6.5 命名约定

```
templateCode:  {bizModule}_{bizType}_{场景}
               例: mall_purchase_default, mall_return_high_amount

bizKey:        {bizModule}:{bizType}:{id}
               例: mall:purchase:10001
```

---

# 七、高可用与性能优化方案

## 7.1 高可用保障

- **集群部署**：`starpivot-approval` 多实例 + Nacos 注册，网关负载均衡
- **熔断限流**：可选 Sentinel，防止待办列表高并发查询压垮 DB
- **幂等消费**：复用 `starpivot-mq` 的 `IdempotentChecker`；消费失败时 `release()` 释放键以便 DLQ 重试；审批完结建议使用业务键 `approval:finished:{instanceId}:{result}`
- **分布式事务**：首期**不引入 Seata**；审批 submit 在 approval 库独立提交，商城本地更新失败时通过 internal withdraw 补偿；完结由 MQ 最终一致
- **并发控制**：`approve` / `reject` 对 `ap_instance` 行加 `SELECT ... FOR UPDATE`，避免会签（ALL）并发卡死

## 7.2 性能优化

- **列表分页**：待办/已办/我发起强制分页
- **索引**：`ap_task(assignee_id, status)`、`ap_instance(biz_module, biz_type)`、`ap_record(instance_id)`
- **异步处理**：完结通知、后续 Phase 3 消息提醒均走 MQ
- **模板缓存**：已发布模板可 Redis 缓存（Phase 3）

---

# 八、安全设计

- **接口鉴权**：网关 JWT + `menu-permission`，与现有 system / mall 一致
- **任务权限**：仅 `assignee_id` 对应用户可 approve/reject
- **撤回权限**：仅发起人、且实例为 RUNNING
- **数据隔离**：待办按当前用户过滤；业务详情页时间轴需校验业务读权限
- **防重复提交**：`ap_instance.uk_biz_running` 唯一约束 + `ensureNoRunningInstance`；商城消费侧仅 `audit_status=PENDING` 接受完结事件
- **审计**：`ap_record` 全程只 INSERT，不可修改

---

# 九、前端规划

现有 `views/workflow/*`（SPF + Vue Flow）**不继续演进**；上线 SAS 后迁移至 `views/approval/`。

| 路由 | 页面 | 说明 |
|------|------|------|
| `/approval/template` | 模板列表 + 步骤编辑 | 表格配置步骤，无画布 |
| `/approval/bind` | 业务绑定规则 | 表单 + SpEL 说明 |
| `/approval/todo` | 待办审批 | 列表 + 审批抽屉 |
| `/approval/mine` | 我发起的 | 列表 + 撤回 |
| 组件 `ApprovalTimeline.vue` | — | 嵌入采购/退货等业务详情页 |

菜单迁移：将 `sys_menu` 154~168 的 `workflow:*` 逐步改为 `approval:*`；路由从 `/workflow/*` 迁到 `/approval/*`。

API 模块：`src/api/approval/template.ts`、`task.ts`、`instance.ts`。

---

# 十、部署与实施计划

## 10.1 模块结构（后端）

```
starpivot-approval/
├── controller/     ApTemplateController, ApTaskController, ApInstanceController
├── service/        ApTemplateService, ApInstanceService, ApTaskQueryService
├── service/engine/ PipelineEngine, AssigneeResolver, RouteEvaluator, SkipEvaluator
├── domain/         entity, dto, vo
├── mapper/
├── mq/             ApprovalFinishedPublisher
└── ApprovalApplication.java
```

在根 `pom.xml` 增加 module；网关增加路由；`starpivot-api` 增加 Feign 契约与 MQ 消息体。

## 10.2 实施阶段

### Phase 1 — 中台 MVP（约 2 周）

- [x] `sql/init_approval.sql` + 菜单 `approval:*`
- [ ] `starpivot-approval` 服务 + 网关路由
- [ ] 模板 CRUD、发布、绑定
- [ ] submit / approve / reject / withdraw / timeline
- [ ] MQ `approval.instance.finished`
- [ ] 前端：`approval/todo`、`approval/mine`、`ApprovalTimeline`

### Phase 2 — 商城接入（约 1 周）

- [x] 首单打通：`mall:purchase`、`mall:return`
- [x] mall：`ApprovalInternalClient` + `MallApprovalFinishedMqListener`（补偿 withdraw、PENDING 状态机）
- [x] 业务页：提交审批 + 嵌入时间轴
- [ ] 逐步替换 OMS 内联 `audit()` 为统一待办；扩展 coupon / spu

### Phase 3 — 增强

- [ ] 审批超时与站内信提醒
- [ ] 转办、加签
- [ ] 驳回到指定步骤
- [ ] 审批统计看板
- [ ] 若 OA 场景爆发（复杂并行/子流程），再评估是否引入 Flowable

## 10.3 部署清单

1. MySQL：执行 `sql/star_pivot.sql` → `sql/sys_menu.sql`；商城五域见 `sql/import_mall_databases.ps1`
2. RabbitMQ：声明 exchange / queue（`starpivot-mq` 拓扑配置）
3. Nacos：注册 `starpivot-approval`
4. Gateway：增加 approval 路由
5. 前端：Nginx 静态资源（与现有一致）

---

# 十一、方案总结

## 11.1 方案优势

- **与 starpivot-cloud 技术栈完全一致**（Boot 3.3、RabbitMQ、Nacos、MyBatis-Plus）
- **单服务 MVP**，开发运维成本可控，参照 file 中台已验证模式
- **阶梯引擎**覆盖商城 90% 审批场景，表结构可 SQL 审计，比 Flowable 轻量
- **MQ 解耦**业务完结，mall / system 等按需接入，无需审批服务反向调用业务

## 11.2 相关文档

| 文档 | 用途 |
|------|------|
| [`docs/doc/workflow-design.md`](doc/workflow-design.md) | 开发规格书（DDL、引擎伪代码、FAQ） |
| [`docs/商城开发事项.md`](商城开发事项.md) | 商城模块与首批接入场景 |

## 11.3 旧方案对照（迁移参考）

| 旧（SPF / 通用 Flowable 方案） | 新（SAS） |
|-------------------------------|-----------|
| `/api/workflow/def/*` | `/api/approval/template/*` |
| `/api/workflow/task/start` | `/api/approval/instance/submit` |
| Flowable deploy | template publish |
| `form_json` 动态表单 | 业务页表单 + `context` |
| RocketMQ + 六微服务 | RabbitMQ + 单服务 MVP |
| `WorkflowCompletedListener` | MQ `ApprovalFinishedMessage` |

---

**文档版本**：2026-06-26 — starpivot-cloud 适配版 v1.0
