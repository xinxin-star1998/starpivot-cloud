# Starpivot Cloud 变更日志

## 版本：2026-07-15（代码审查跟进）

---

### 审查纠正（相对上一版）

1. **GlobalExceptionHandler** — 业务异常（含 `BIZ_ERROR`）保持 **HTTP 200 + body.code**；仅 `SYSTEM_ERROR` 用 HTTP 500，避免前端对业务失败误重试。
2. **AuthGlobalFilter 黑名单** — 维持 **Fail-Closed**（Redis 异常拒绝请求）；CHANGELOG 此前「降级放行」描述作废。
3. **前端 refresh** — 登出重置 `refreshRetryCount`，刷新失败清空等待队列。
4. **SysUserAuthSupport** — JWT `roles` / `roleList` 仅包含启用角色（`status=0`），防止停用 admin 仍进令牌。

### 并发与一致性

5. **优惠券领取** — `SELECT … FOR UPDATE` 串行化同券领取，堵住 perLimit TOCTOU。
6. **会员等级** — 成长值变更后仅 `UPDATE level_id`，避免整实体覆盖并发积分/成长。
7. **积分扣减/回滚** — 区分「会员不存在」与「积分不足」；回滚前校验会员存在。
8. **订单 Outbox** — `flushMessage` 与 `flushPending` 统一 claim 后再投递。

### 审批完结 Outbox

9. **ApprovalFinishedPublisher** — 事务内写 `mq_message`，提交后 flush；定时补偿。
10. **DDL** — `sql/star_pivot_approval.sql` 含表；存量库执行 `sql/patch_approval_mq_message.sql`。

### 安全配置

11. **默认密码** — 代码侧无弱默认；须配置 `STAR_PIVOT_DEFAULT_PASSWORD`。
12. **JWT 热更新** — `JwtSecretRefreshAutoConfiguration` 在 `starpivot.jwt.*` 变更时调用 `JwtUtils.clearCache()`。

---

## 版本：2026-07-14

---

## 🔴 P0 - 严重问题修复

### 1. 敏感数据硬编码修复

**问题描述**：默认密码 `"Star123456"` 直接硬编码在代码中，存在安全风险

**修改文件**：
- **新建** `starpivot-system/.../SysAccountProperties.java`
- **修改** `SysUserServiceImpl`、`nacos/config/starpivot-system.yaml`

**变更说明**：
- 默认密码改为从配置读取，支持环境变量 `STAR_PIVOT_DEFAULT_PASSWORD`
- **（07-15）** 代码与 Nacos 已去掉弱默认值，未配置时新建用户会失败并提示设置环境变量

---

### 2. SecurityUtils 混用修复

统一使用实例注入方式调用 `SecurityUtils`，确保密码加密策略一致。

---

## 🟡 P1 - 中等问题修复

### 3. GlobalExceptionHandler HTTP 状态码

**（07-15 已纠正）**：
- `SYSTEM_ERROR` → HTTP 500
- 其余业务错误（含 `BIZ_ERROR`）→ **HTTP 200 + body.code**
- 原有 401/403/404 保持不变

---

### 4. Redis 黑名单策略

**（07-15 以代码为准）**：**Fail-Closed** — Redis 异常时拒绝请求。需配合 Redis 监控。

---

## 🟢 P2 - 建议优化

### 5. AuthService.buildRoles 使用真实角色数据

- 真实 `SysRoleInfoDto`；**（07-15）** 认证 roleKey 仅含启用角色

### 6. JwtUtils 密钥缓存

- `clearCache()` + `JwtSecretRefreshAutoConfiguration` 监听 `starpivot.jwt.*` 自动清缓存

---

## 🔵 P3 - 性能优化

### 7. AsyncConfig 线程池可配置（`starpivot.async.*`，默认按 CPU 推算）

### 8. 前端刷新令牌重试限制 + 登出清理

---

## ✅ 部署注意

1. 存量审批库执行：`sql/patch_approval_mq_message.sql`
2. 设置环境变量：`STAR_PIVOT_DEFAULT_PASSWORD`
3. 本地编译抽查：`mvn -pl starpivot-common,starpivot-system,starpivot-approval -am compile -DskipTests`
