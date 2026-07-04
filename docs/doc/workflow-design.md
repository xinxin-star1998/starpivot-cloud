# 审批中台设计与开发文档（SAS）

> **SAS（StarPivot Approval Service）**：面向 `starpivot-cloud` 微服务架构的**业务审批中台**，与 StarPivot 单体里的 SPF 图引擎方案**完全独立**。
>
> 设计原则：**表结构归一化、阶梯流水线、事件驱动完结、业务服务弱耦合**。

---

## 1. 为什么要重新设计

| 维度 | SPF 图引擎（弃用） | SAS 审批中台（本方案） |
|------|-------------------|------------------------|
| 定义形态 | 大块 `defJson` + 编译 `runtimeJson` | 模板 + 步骤表，可查询、可 diff |
| 建模方式 | 自由画布（start/approval/condition/end） | **有序审批阶梯** + 可选条件路由 |
| 引擎复杂度 | 自研图遍历 + 编译器 | 线性/规则跳转，SpEL 表达式 |
| 微服务协作 | Listener 同进程回调 | **MQ 完结事件**，业务服务消费 |
| 前端 | Vue Flow 设计器 | 步骤配置表 + 时间轴进度（更贴合 B 端表单） |
| 运维 | 流程图难审计 | 步骤、记录表可直接 SQL 排查 |

现有 `star-pivot-ui/src/views/workflow/`（SPF 设计器）**不纳入本方案**；上线 SAS 后逐步替换为 `views/approval/`（见 §10）。

---

## 2. 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│ 业务服务（starpivot-mall-* / system / …）                          │
│  提交单据 → ApprovalInternalClient.submit()（/internal/**）      │
│  消费 MQ → MallApprovalFinishedMqListener 回写 audit_status      │
└───────────────┬───────────────────────────────┬─────────────────┘
                │ Feign 直连（X-Internal-Token）  │ RabbitMQ（异步）
                ▼                               ▼
┌───────────────────────────┐     ┌────────────────────────────┐
│ Gateway /api/v1/approval/**│     │ starpivot.topic             │
└───────────────┬───────────┘     │ routing: approval.finished  │
                ▼                 └──────────────┬───────────────┘
┌───────────────────────────────────────────────┴─────────────────┐
│ starpivot-approval（独立微服务）                                 │
│  TemplateService / InstanceService / TaskService                  │
│  PipelineEngine（阶梯推进）                                       │
│  AssigneeResolver（对接 system 用户/部门/角色/岗位）                │
│  MySQL ap_* 表                                                    │
└─────────────────────────────────────────────────────────────────┘
```

**职责边界**

- **审批中台**：谁审批、第几关、通过/驳回、留痕；不关心订单怎么发货、采购怎么入库。
- **业务服务**：单据 CRUD、业务状态机、库存/支付等；收到完结事件后更新自己的 `audit_status`。

---

## 3. 核心概念

| 概念 | 字段示例 | 说明 |
|------|----------|------|
| `bizModule` | `mall` | 业务域，与现有模块划分一致 |
| `bizType` | `purchase` | 业务单据类型 |
| `bizKey` | `mall:purchase:10001` | 全局业务键，`模块:实体:ID` |
| `templateCode` | `mall_purchase_default` | 审批模板编码 |
| `instanceId` | `90001` | 一次审批实例（一单一次） |
| `taskId` | `80001` | 某一审批节点上的待办 |
| `context` | `{ "amount": 12800 }` | 审批上下文，供 SpEL 条件与展示 |

**模板匹配**：业务提交时不强制传 `templateCode`，由 `ap_template_bind` 按 `bizModule + bizType + 条件` 自动匹配；也支持调用方显式指定。

---

## 4. 审批流水线模型

### 4.1 基本形态

默认是 **有序阶梯**（Step 1 → Step 2 → … → 完成）：

```
提交 → [步骤1：部门负责人] → [步骤2：财务] → [步骤3：总监] → 通过
                ↓ 驳回（任意步骤）
              整单结束（REJECTED）
```

每个步骤配置：

- **审批人策略**（见 §4.3）
- **通过模式**：`ANY`（或签）/ `ALL`（会签）
- **可选跳过表达式**：满足则本步不生成待办，直接进入下一步

### 4.2 条件路由（替代 SPF 条件节点）

在**某步骤完成后**，按优先级评估 `ap_template_route`，决定下一步 `to_step_id`：

```
步骤「金额判断」完成后：
  1. #context['amount'] > 10000  → 步骤「总监审批」
  2. default                       → 步骤「普通审批」
```

无匹配且无 default 时，按 `step_order + 1` 线性前进。

条件语言：**Spring SpEL**，上下文根对象为 `context`（Map）。

### 4.3 审批人策略 assigneeType

| 类型 | 配置值 | 说明 |
|------|--------|------|
| `STARTER` | — | 发起人本人 |
| `DEPT_LEADER` | — | 发起人部门负责人（`sys_dept.leader`） |
| `ROLE` | `roleKey` | 角色下启用用户 |
| `POST` | `postCode` | 岗位下启用用户 |
| `USER` | 用户 ID | 指定用户 |
| `DEPT_ROLE` | `deptId:roleKey` | 指定部门 + 角色（扩展） |

解析失败：**提交/推进失败**，返回明确错误（不静默跳过），避免无人审批。

### 4.4 通过 / 驳回语义

| 操作 | 实例状态 | 业务含义 |
|------|----------|----------|
| 通过（当前步完成且还有下一步） | `RUNNING` | 生成下一步待办 |
| 通过（最后一步完成） | `APPROVED` | 发 MQ `APPROVED` |
| 驳回 | `REJECTED` | 取消全部待办，发 MQ `REJECTED` |
| 撤回 | `WITHDRAWN` | 仅发起人、且 `RUNNING`，发 MQ `WITHDRAWN` |
| 超时（可选 Phase 2） | `TIMEOUT` | 策略可配置：自动驳回或升级 |

**不支持**（首期）：驳回到上一节点、加签、转办、抄送、子流程。

---

## 5. 数据库设计

脚本路径：`sql/init_approval.sql`（含 DDL、商城默认模板及忘记密码开关）。

### 5.1 表清单

| 表 | 说明 |
|----|------|
| `ap_template` | 审批模板主表 |
| `ap_template_step` | 模板步骤（有序） |
| `ap_template_route` | 步骤完成后的条件路由（可选） |
| `ap_template_bind` | 业务类型与模板绑定 |
| `ap_instance` | 审批实例 |
| `ap_task` | 待办任务 |
| `ap_record` | 审批记录（不可变流水） |

### 5.2 建表 DDL（核心）

```sql
-- 审批模板
CREATE TABLE `ap_template` (
  `template_id`     bigint       NOT NULL AUTO_INCREMENT,
  `template_code`   varchar(64)  NOT NULL COMMENT '模板编码',
  `template_name`   varchar(128) NOT NULL,
  `biz_module`      varchar(32)  NOT NULL COMMENT 'mall/system/…',
  `version`         int          NOT NULL DEFAULT 1,
  `status`          varchar(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DISABLED',
  `remark`          varchar(500) NULL,
  `create_by`       varchar(64)  NULL,
  `create_time`     datetime     NULL,
  `update_by`       varchar(64)  NULL,
  `update_time`     datetime     NULL,
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_code_version` (`template_code`, `version`),
  KEY `idx_module_status` (`biz_module`, `status`)
) COMMENT='审批模板';

-- 模板步骤（阶梯）
CREATE TABLE `ap_template_step` (
  `step_id`           bigint       NOT NULL AUTO_INCREMENT,
  `template_id`       bigint       NOT NULL,
  `step_code`         varchar(64)  NOT NULL COMMENT '步骤编码，实例内唯一',
  `step_name`         varchar(128) NOT NULL,
  `step_order`        int          NOT NULL COMMENT '排序，从 1 递增',
  `assignee_type`     varchar(32)  NOT NULL,
  `assignee_value`    varchar(128) NULL,
  `approve_mode`      varchar(8)   NOT NULL DEFAULT 'ANY' COMMENT 'ANY/ALL',
  `skip_expression`   varchar(512) NULL COMMENT 'SpEL，true 则跳过本步',
  `create_time`       datetime     NULL,
  PRIMARY KEY (`step_id`),
  UNIQUE KEY `uk_tpl_step_code` (`template_id`, `step_code`),
  KEY `idx_tpl_order` (`template_id`, `step_order`)
) COMMENT='审批模板步骤';

-- 条件路由（某步完成后选下一步）
CREATE TABLE `ap_template_route` (
  `route_id`        bigint       NOT NULL AUTO_INCREMENT,
  `template_id`     bigint       NOT NULL,
  `from_step_id`    bigint       NOT NULL,
  `priority`        int          NOT NULL DEFAULT 0 COMMENT '越小优先',
  `condition_expr`  varchar(512) NULL COMMENT 'SpEL，空或 default 表示默认分支',
  `to_step_id`      bigint       NOT NULL,
  PRIMARY KEY (`route_id`),
  KEY `idx_tpl_from` (`template_id`, `from_step_id`, `priority`)
) COMMENT='审批步骤路由';

-- 业务绑定：哪种单据走哪个模板
CREATE TABLE `ap_template_bind` (
  `bind_id`       bigint       NOT NULL AUTO_INCREMENT,
  `biz_module`    varchar(32)  NOT NULL,
  `biz_type`      varchar(64)  NOT NULL COMMENT 'purchase/return/coupon…',
  `match_expr`    varchar(512) NULL COMMENT 'SpEL，空=总是匹配',
  `template_code` varchar(64)  NOT NULL COMMENT '指向已发布模板',
  `priority`      int          NOT NULL DEFAULT 0,
  `status`        char(1)      NOT NULL DEFAULT '0' COMMENT '0启用 1停用',
  PRIMARY KEY (`bind_id`),
  KEY `idx_biz` (`biz_module`, `biz_type`, `status`, `priority`)
) COMMENT='审批模板业务绑定';

-- 审批实例
CREATE TABLE `ap_instance` (
  `instance_id`     bigint       NOT NULL AUTO_INCREMENT,
  `template_id`     bigint       NOT NULL,
  `template_code`   varchar(64)  NOT NULL,
  `biz_module`      varchar(32)  NOT NULL,
  `biz_type`        varchar(64)  NOT NULL,
  `biz_key`         varchar(128) NOT NULL,
  `title`           varchar(256) NOT NULL,
  `starter_id`      bigint       NOT NULL,
  `status`          varchar(16)  NOT NULL DEFAULT 'RUNNING',
  `current_step_id` bigint       NULL COMMENT '当前步骤',
  `context_json`    json         NULL COMMENT '审批上下文',
  `create_time`     datetime     NULL,
  `finish_time`     datetime     NULL,
  PRIMARY KEY (`instance_id`),
  UNIQUE KEY `uk_biz_running` (`biz_key`, `status`) COMMENT '仅允许一个 RUNNING，实现时用应用层+索引配合',
  KEY `idx_starter` (`starter_id`),
  KEY `idx_biz` (`biz_module`, `biz_type`)
) COMMENT='审批实例';

-- 待办任务
CREATE TABLE `ap_task` (
  `task_id`       bigint       NOT NULL AUTO_INCREMENT,
  `instance_id`   bigint       NOT NULL,
  `step_id`       bigint       NOT NULL,
  `step_code`     varchar(64)  NOT NULL,
  `step_name`     varchar(128) NOT NULL,
  `assignee_id`   bigint       NOT NULL,
  `status`        varchar(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/CANCELLED',
  `action`        varchar(16)  NULL COMMENT 'APPROVE/REJECT',
  `comment`       varchar(500) NULL,
  `create_time`   datetime     NULL,
  `finish_time`   datetime     NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_assignee_status` (`assignee_id`, `status`),
  KEY `idx_instance` (`instance_id`)
) COMMENT='审批待办';

-- 审批记录（审计流水，只 INSERT）
CREATE TABLE `ap_record` (
  `record_id`     bigint       NOT NULL AUTO_INCREMENT,
  `instance_id`   bigint       NOT NULL,
  `task_id`       bigint       NULL,
  `step_code`     varchar(64)  NOT NULL,
  `step_name`     varchar(128) NOT NULL,
  `operator_id`   bigint       NOT NULL,
  `action`        varchar(16)  NOT NULL COMMENT 'SUBMIT/APPROVE/REJECT/WITHDRAW/SKIP',
  `comment`       varchar(500) NULL,
  `create_time`   datetime     NOT NULL,
  PRIMARY KEY (`record_id`),
  KEY `idx_instance` (`instance_id`)
) COMMENT='审批记录';
```

> **uk_biz_running 说明**：MySQL 对 `status` 多值唯一约束较弱，推荐业务层保证「同 `biz_key` 仅一个 `RUNNING`」，或增加 `running_flag` 生成列做唯一索引。

---

## 6. 引擎：PipelineEngine

### 6.1 状态机（实例）

```
SUBMIT → RUNNING ⇄ (步骤推进) → APPROVED
              ↘ REJECTED
              ↘ WITHDRAWN
```

### 6.2 推进算法（伪代码）

```
submit(bizModule, bizType, bizKey, title, context, starterId):
  1. 校验无 RUNNING 实例（同 bizKey）
  2. resolveTemplate(bind 或显式 templateCode)
  3. insert ap_instance (RUNNING)
  4. insert ap_record SUBMIT
  5. enterStep(firstStep)

enterStep(step):
  if skip_expression 为 true → record SKIP → advanceFrom(step)
  resolve assignees → insert ap_task (PENDING) 每条
  update instance.current_step_id

approve(taskId, comment, operatorId):
  校验 assignee、PENDING
  完成 task → record APPROVE
  if approve_mode == ALL and 同 step 仍有 PENDING → return
  if approve_mode == ANY → 取消同 step 其他 PENDING
  advanceFrom(currentStep)

advanceFrom(step):
  next = evaluateRoutes(step, context) ?? stepOrder+1
  if next 为空 → finish(APPROVED)
  else enterStep(next)

reject(taskId, comment, operatorId):
  完成 task → 取消全部 PENDING → instance REJECTED → publish MQ

withdraw(instanceId, starterId):
  校验发起人 → 取消待办 → WITHDRAWN → publish MQ
```

### 6.3 与 system 模块集成

`AssigneeResolver` 通过 Feign 调用 `starpivot-system` 的 `/internal/org/**`（`SysOrgClient`），解析用户/部门负责人/角色/岗位。**不**在审批服务内复制组织数据或直连 `sys_*` 表。

---

## 7. 微服务与 MQ

### 7.1 服务：`starpivot-approval`

| 项 | 值 |
|----|-----|
| 服务名 | `starpivot-approval` |
| context-path | `/api/v1` |
| Controller 前缀 | `/approval/**` |
| 注册 | Nacos `lb://starpivot-approval` |

**网关路由（待加）**

```yaml
- id: starpivot-approval
  uri: lb://starpivot-approval
  predicates:
    - Path=/api/${starpivot.api.version}/approval/**
```

### 7.2 完结事件（推荐主集成方式）

在 `starpivot-api` 增加契约：

```java
// MqRoutingKeys
public static final String APPROVAL_INSTANCE_FINISHED = "approval.instance.finished";
// 商城按 bizType 路由：approval.instance.finished.mall.{purchase|spu|return|coupon}

// MqQueueNames（各商城微服务独立消费队列）
public static final String MALL_APPROVAL_FINISHED_PURCHASE = "starpivot.mall.approval-finished.purchase";
public static final String MALL_APPROVAL_FINISHED_SPU = "starpivot.mall.approval-finished.spu";
public static final String MALL_APPROVAL_FINISHED_RETURN = "starpivot.mall.approval-finished.return";
public static final String MALL_APPROVAL_FINISHED_COUPON = "starpivot.mall.approval-finished.coupon";
```

**消息体 `ApprovalFinishedMessage`**

```json
{
  "instanceId": 90001,
  "bizModule": "mall",
  "bizType": "purchase",
  "bizKey": "mall:purchase:10001",
  "templateCode": "mall_purchase_default",
  "result": "APPROVED",
  "starterId": 1,
  "comment": null,
  "finishTime": "2026-06-26T10:00:00",
  "traceId": "…"
}
```

`result`：`APPROVED` | `REJECTED` | `WITHDRAWN`

业务服务消费示例（mall，继承 `AbstractMqListener`）：

```java
public class MallApprovalFinishedMqListener extends AbstractMqListener<ApprovalFinishedMessage> {

    @Override
    protected String resolveIdempotentKey(Message message, MessageEnvelope<ApprovalFinishedMessage> envelope) {
        ApprovalFinishedMessage p = envelope.getPayload();
        return "approval:finished:" + p.getInstanceId() + ":" + p.getResult();
    }

    private void dispatch(ApprovalFinishedMessage msg) {
        purchaseApprovalService.handleApprovalFinished(...);  // 内部校验 bizType + PENDING
        returnApprovalService.handleApprovalFinished(...);
    }
}
```

**原则**：Feign 用于 `submit`、查进度等同步操作；**完结通知走 MQ**，避免审批服务反向依赖业务服务。

**发送保证**：`ApprovalFinishedPublisher` 使用 `afterCommit` 发送；系统补偿 `withdraw` 不发 MQ。

**消费保证**：`handleApprovalFinished` 仅当 `audit_status=PENDING` 时更新；幂等键 `approval:finished:{instanceId}:{result}`。

### 7.3 starpivot-api 客户端

**服务间提交 / 补偿（mall 等业务服务）**：

```java
@FeignClient(name = "starpivot-approval", contextId = "approvalInternalClient")
public interface ApprovalInternalClient {
    @PostMapping("/internal/approval/instance/submit")
    Result<Long> submit(@RequestBody InternalApprovalSubmitRequest request);

    @PostMapping("/internal/approval/instance/{id}/withdraw")
    Result<Void> withdraw(@PathVariable("id") Long instanceId);
}
```

**用户侧查询（可选，经网关 JWT）**：

```java
@FeignClient(name = "starpivot-approval", contextId = "approvalClient")
public interface ApprovalClient {
    @GetMapping("/approval/instance/{id}/timeline")
    Result<ApprovalTimelineVo> timeline(@PathVariable("id") Long id);
}
```

各商城微服务（`starpivot-mall-*`）引入 `starpivot-api` + OpenFeign 即可，**无需**依赖审批服务实现 jar。内部接口需配置 `starpivot.internal.token`（各服务一致）。

---

## 8. HTTP API

前缀：`/api/v1/approval`（网关外部路径）。

### 8.1 模板管理（管理端）

| 功能 | 方法 | 路径 | 权限建议 |
|------|------|------|----------|
| 模板分页 | POST | `/template/templatePageList` | `approval:template:query` |
| 模板详情（含步骤） | GET | `/template/{id}` | `approval:template:query` |
| 保存模板+步骤 | POST | `/template/save` | `approval:template:edit` |
| 发布 | POST | `/template/{id}/publish` | `approval:template:publish` |
| 停用 | POST | `/template/{id}/disable` | `approval:template:edit` |
| 绑定规则分页 | POST | `/template/templateBindPageList` | `approval:bind:edit` |
| 保存绑定规则 | POST | `/template/bind/save` | `approval:bind:edit` |
| 删除绑定规则 | DELETE | `/template/removeTemplateBind` | `approval:bind:edit` |

### 8.2 实例与任务（业务 + 审批人）

| 功能 | 方法 | 路径 | 权限建议 |
|------|------|------|----------|
| 提交审批 | POST | `/instance/submit` | `approval:instance:submit`（管理端）；业务服务走 §8.2.1 |
| 待办列表 | POST | `/task/todoTaskPageList` | `approval:task:query` |
| 已办列表 | POST | `/task/doneTaskPageList` | `approval:task:query` |
| 我发起的 | POST | `/instance/mineInstancePageList` | `approval:instance:query` |
| 通过 | POST | `/task/approve` | `approval:task:action` |
| 驳回 | POST | `/task/reject` | `approval:task:action` |
| 撤回 | POST | `/instance/{id}/withdraw` | `approval:instance:withdraw` |
| 审批时间轴 | GET | `/instance/{id}/timeline` | `approval:instance:query` |

### 8.2.1 内部接口（Feign 直连，不经网关）

| 功能 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 服务间提交 | POST | `/internal/approval/instance/submit` | `InternalApprovalSubmitRequest`，含 `starterId` |
| 系统补偿撤回 | POST | `/internal/approval/instance/{id}/withdraw` | 不发 MQ；用于商城本地落库失败补偿 |

鉴权：`InternalServiceAuthFilter` + `X-Internal-Token`。网关禁止访问 `/internal/**`。

### 8.3 提交请求体

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

`templateCode` 为空时走 `ap_template_bind` 自动匹配。

### 8.4 时间轴响应（供前端 Step 组件）

```json
{
  "instanceId": 90001,
  "title": "采购单审批 #10001",
  "status": "RUNNING",
  "steps": [
    {
      "stepCode": "dept_leader",
      "stepName": "部门负责人",
      "status": "DONE",
      "records": [
        { "operatorName": "张三", "action": "APPROVE", "comment": "同意", "time": "…" }
      ]
    },
    {
      "stepCode": "finance",
      "stepName": "财务审批",
      "status": "PENDING",
      "assignees": ["李四"],
      "records": []
    }
  ]
}
```

---

## 9. 业务接入规范

### 9.1 业务表字段建议

| 字段 | 说明 |
|------|------|
| `approval_instance_id` | 关联 `ap_instance.instance_id` |
| `audit_status` | `DRAFT` / `PENDING` / `APPROVED` / `REJECTED` / `WITHDRAWN` |

### 9.2 命名约定

```
templateCode:  {bizModule}_{bizType}_{场景}
               例: mall_purchase_default, mall_return_high_amount

bizKey:        {bizModule}:{bizType}:{id}
               例: mall:purchase:10001
```

### 9.3 商城首批接入场景

| bizType | 说明 | 阶梯示例 |
|---------|------|----------|
| `purchase` | 采购单 | 部门负责人 → 财务（amount>1万加总监） |
| `return` | 退货申请 | 客服主管 → 财务（替代页面内直接 audit） |
| `coupon` | 优惠券发布 | 运营主管 → 财务 |
| `spu` | 商品上架 | 品类负责人 → 质控 |

### 9.4 提交与消费完整流程

```
1. 用户点击「提交审批」（mall 校验 mall:purchase:edit 等业务权限）
2. mall 更新 audit_status=PENDING
3. ApprovalInternalClient.submit(starterId=当前用户)
4. 本地写入 approval_instance_id；失败则 internal withdraw 补偿 + 回滚 audit_status
5. 审批人在中台待办 approve/reject（实例行 FOR UPDATE）
6. approval 事务提交后发 MQ approval.instance.finished
7. mall 消费 MQ：幂等键 instanceId+result；仅 PENDING 时更新 audit_status + 后续业务
```

### 9.5 配置 checklist（服务间调用）

| 配置项 | 说明 |
|--------|------|
| `starpivot.internal.token` | 各微服务相同，Feign 调 `/internal/**` 时携带 |
| `starpivot.mq.enabled=true` | approval、mall 均需启用 MQ |
| Redis | mall 消费端幂等依赖 `RedisIdempotentChecker` |

---

## 10. 前端规划（替代现有 SPF 页面）

现有 `views/workflow/*` 基于 SPF + Vue Flow，**不继续演进**。

### 10.1 新目录 `views/approval/`

| 路由 | 页面 | 说明 |
|------|------|------|
| `/approval/template` | 模板列表 + 步骤编辑 | 表格配置步骤，无画布 |
| `/approval/bind` | 业务绑定规则 | 表单 + SpEL 说明 |
| `/approval/todo` | 待办审批 | 列表 + 审批抽屉 |
| `/approval/mine` | 我发起的 | 列表 + 撤回 |
| `/approval/timeline/:id` | 审批详情 | 垂直时间轴 |

### 10.2 可复用组件

- `ApprovalTimeline.vue` — 步骤条 + 记录列表（嵌入业务详情页）
- `ApprovalActionDrawer.vue` — 通过/驳回 + 意见

### 10.3 API 模块

`src/api/approval/template.ts`、`task.ts`、`instance.ts`（路径 `/api/approval/...`）。

### 10.4 菜单权限迁移

将 `sys_menu` 154~168 的 `workflow:*` 逐步改为 `approval:*`；路由从 `/workflow/*` 迁到 `/approval/*`。可与后端同期上线。

---

## 11. 模块结构（后端）

```
starpivot-approval/
├── controller/
│   ├── ApTemplateController
│   ├── ApTaskController
│   ├── ApInstanceController
│   └── internal/
│       └── ApInstanceInternalController   # /internal/approval/**
├── service/
│   ├── ApTemplateService
│   ├── ApInstanceService
│   ├── ApTaskQueryService
│   └── engine/
│       ├── PipelineEngine                 # FOR UPDATE + afterCommit MQ
│       ├── AssigneeResolver
│       ├── TemplateResolver
│       └── SpelEvaluator
├── domain/ entity, dto, vo
├── mapper/
│   └── ApInstanceMapper                   # selectByIdForUpdate
├── mq/
│   └── ApprovalFinishedPublisher          # afterCommit 发送
└── ApprovalApplication.java
```

**依赖**：`starpivot-common`、`starpivot-api`、`starpivot-mq`、`spring-cloud-starter-openfeign`；组织数据仅通过 Feign 调 `starpivot-system` internal API（需配置 `INTERNAL_SERVICE_TOKEN`）。

---

## 12. 实施阶段

### Phase 1 — 中台 MVP（约 2 周）

- [x] `sql/init_approval.sql` + 菜单 `approval:*`
- [ ] `starpivot-approval` 服务 + 网关路由
- [ ] 模板 CRUD、发布、绑定
- [ ] submit / approve / reject / withdraw / timeline
- [ ] MQ `approval.instance.finished`
- [ ] 前端：`approval/todo`、`approval/mine`、`ApprovalTimeline`

### Phase 2 — 商城接入（约 1 周）

- [x] `mall:purchase` / `mall:return` 打通
- [x] Feign `ApprovalInternalClient` + `MallApprovalFinishedMqListener`
- [x] 采购单 / 退货页：提交审批 + 嵌入时间轴

### Phase 3 — 增强

- [ ] 审批超时与提醒（站内信 / 定时任务）
- [ ] 转办、加签
- [ ] 驳回到指定步骤（需引擎扩展）
- [ ] 审批统计看板

---

## 13. 安全与权限

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
| `approval:instance:submit` | 管理端通用提交；**业务服务走 internal API，见 §8.2.1** |

业务服务提交不要求用户具备 `approval:instance:submit`；详见 [`docs/security/permission-strategy.md`](../security/permission-strategy.md)。

数据权限（可选 Phase 3）：待办列表按部门数据范围过滤，复用 system 数据权限注解。

---

## 14. 常见问题

| 问题 | 处理 |
|------|------|
| 提交报「无匹配模板」 | 检查 `ap_template_bind`、模板是否 `PUBLISHED` |
| 提交报「已有进行中审批」 | 同 `bizKey` 存在 `RUNNING`，需撤回或等结束 |
| 审批人解析失败 | 检查部门 leader、角色/岗位用户 |
| 条件路由不生效 | 检查 SpEL、`context` 字段名与类型 |
| 业务状态未更新 | 查 MQ 消费日志、`audit_status` 是否为 PENDING、Redis 幂等 |
| mall 提交审批 403 | 应使用 `ApprovalInternalClient` 而非 `ApprovalClient.submit` |
| internal 401 | 各服务 `starpivot.internal.token` 不一致或未配置 |
| 会签流程卡住 | 确认 `PipelineEngine` 已对实例 `FOR UPDATE`；查并发审批日志 |

---

## 15. 与旧 SPF 前端/API 对照

| 旧（SPF） | 新（SAS） |
|-----------|-----------|
| `/api/workflow/def/*` | `/api/approval/template/*` |
| `/api/workflow/task/start` | `/api/approval/instance/submit`（管理端）或 `/internal/approval/instance/submit`（服务间） |
| `/api/workflow/task/approve` | `/api/approval/task/approve` |
| `defJson` 画布 | `ap_template_step` 表 |
| `WorkflowCompletedListener` | MQ `ApprovalFinishedMessage` |
| `FlowProgressViewer` | `ApprovalTimeline` |

---

**文档版本**：2026-06-26 — SAS 审批中台初版设计，独立于 StarPivot SPF。
