# 全平台消息中心

## 能力概览

| 能力 | 说明 |
|------|------|
| 统一收件箱 | `star_pivot.sys_user_message`，登录用户可查/已读 |
| 顶栏铃铛 | 最近 10 条预览 + 未读角标，点击「查看全部」进入消息中心 |
| 业务自动通知 | 审批、退款失败等通过 Feign 投递 |
| 管理员群发 | `POST /api/v1/message/broadcast`，权限 `system:message:send` |
| 实时推送 | SSE `GET /api/v1/message/sse/subscribe` + Redis Pub/Sub |

## 菜单与权限

| menu_id | 名称 | perms |
|---------|------|-------|
| 319 | 消息中心 | `system:message:list` |
| 320 | 消息查询 | `system:message:query` |
| 321 | 消息已读 | `system:message:edit` |
| 322 | 消息群发 | `system:message:send` |

群发接口使用 `@ss.hasPermission('system:message:send')`：**超级管理员**、**admin 角色**直接放行；其他用户需在角色中勾选「消息群发」菜单。

## 业务接入

### 指定用户

```java
MessageSendRequest req = new MessageSendRequest();
req.setUserIds(List.of(userId));
req.setMsgType(MessageConstants.MSG_TYPE_ORDER);
req.setTitle("标题");
req.setContent("内容");
req.setBizModule(MessageConstants.BIZ_MODULE_MALL);
req.setBizType("order");
req.setBizKey("mall:order:123");
req.setBizId(123L);
req.setLinkPath("/mall/oms/order/order");
sysMessageClient.send(req);
```

### 按角色（内部）

```java
MessageSendToRolesRequest req = new MessageSendToRolesRequest();
req.setRoleKeys(List.of("finance", "admin"));
req.setMsgType(MessageConstants.MSG_TYPE_REFUND_ALERT);
// ... 同上字段
sysMessageClient.sendToRoles(req);
```

## 已接入场景

| 场景 | 模块 | msg_type |
|------|------|----------|
| 审批待办/完结 | starpivot-approval | APPROVAL_TASK / APPROVAL_RESULT |
| 退款失败告警 | starpivot-mall-order | REFUND_ALERT → finance、admin 角色 |
| 管理员群发 | 管理端消息中心 | BROADCAST |

## 部署

1. 执行 `sql/patch_sys_user_message.sql`（表 + 菜单 319–322 + admin 角色菜单绑定）
2. 历史数据（可选）：`sql/patch_migrate_ap_notification_to_sys_user_message.sql`
3. 重启 `starpivot-system`、`starpivot-approval`、`starpivot-mall-order`、网关
4. **重新登录**刷新菜单、权限缓存与 SSE 连接

## 实时推送说明

- 服务端落库后发布 Redis 频道 `starpivot:message:push`
- 各 system 实例订阅后向本机 SSE 连接推送
- 前端顶栏登录后自动连接 `/api/message/sse/subscribe`（Bearer Token）
- 网关需保持长连接，生产环境注意 SSE 超时配置

## 管理端页面

- 路由：`/system/message/index`（系统管理 → 消息中心）
- 筛选：已读状态、消息类型（卡片外搜索栏）
- 操作：群发消息、全部已读；未读行高亮，点击行标记已读并跳转业务页
