---
name: starpivot-code-review
description: >-
  Review selected or changed StarPivot Cloud code against project-root .cursorrules.
  Checks coding standards, bugs/edge cases/performance risks, frontend-backend API
  consistency, and suggests project-style fixes with risk-graded findings and file paths.
  Use when the user asks to review selected code, 代码审查, 规范审查, 按 cursorrules 审查,
  or check API consistency between frontend and backend.
---

# StarPivot 选中代码审查

基于仓库根目录 [`.cursorrules`](../../../.cursorrules) 对用户选中（或当前关注）的代码做审查。规范以该文件为准；本 skill 只定义审查流程与输出格式，禁止另起一套外部风格体系。

## 何时启用

- 用户选中代码并要求审查 / 合规检查 / 找 bug
- 用户要求对照 `.cursorrules` 或「本项目规范」审查
- 用户要求核对前后端接口是否一致

## 审查前必做

1. **读取** 项目根目录 `.cursorrules`（完整阅读；不得凭记忆臆造规范）。
2. 确定审查范围：优先用户选中片段；必要时读写同模块邻近文件以判断「就近一致」。
3. 若涉及接口，同时打开对应端：
   - 后端：`Controller` / DTO·VO·ReqBo / `Result`·`PageResponse`
   - 前端：`src/api/**` 的 `fetch*`、类型、`@/utils/http` 调用
   - 禁止假定字段名「纠正」；已知历史不一致（如 `username` vs `userName`、`phoneNumber` vs `phonenumber`）以契约为准。

## 审查维度（必须覆盖）

按下列四点逐项完成：

1. **检查是否违反项目编码规范**
2. **查找bug、边界条件、性能隐患**
3. **前后端接口一致性校验**
4. **给出符合本项目风格的优化代码**

风险分级输出，标注文件路径。

### 1. 编码规范（对照 .cursorrules）

按改动所在端抽查，重点包括但不限于：

**后端**：分层与包名；Controller 相对路径（禁止硬编码 `/api/v1`）；`Result` / `PageResponse`；`BizException`（禁用废弃异常）；业务失败 HTTP 语义；`@PreAuthorize` / `/internal/**`；Feign 仅在 `starpivot-api`；写事务发件箱；中文校验文案；模块内 VO/DTO 命名就近一致。

**前端**：`<script setup lang="ts">`；`fetch*` + `@/utils/http`；Admin / Portal Token 不混用；分页 `pageNum`/`pageSize` 与 `rows`/`total`；金额走 `@/utils/mall/money`；权限串与后端一致；Prettier 习惯（单引号、无分号等）。

**总则**：最小改动；不建议为「风格统一」做跨模块大改。

### 2. Bug / 边界 / 性能

- 空值、并发、幂等、状态机非法跳转、事务边界
- 分页/批量无上限、N+1、循环内远程调用或重复查库
- 金额精度、枚举/字典 code 类型不一致
- 鉴权遗漏、`/internal` 暴露风险、明文密钥

### 3. 前后端接口一致性

仅在审查范围触及 API 时执行；否则注明「本次无接口面，跳过」。

核对：路径受众（admin `/mall|sys|...` vs portal `/portal`）；HTTP 方法；请求/响应字段同名同形；分页契约；成功码 `200` + `data`；业务错误 HTTP 200 + `code ≠ 200` 的前端处理；权限串。

### 4. 优化代码

- 只给与问题相关的最小补丁，贴合本模块既有写法
- 用 `startLine:endLine:filepath` 引用原代码；优化示例用普通 fenced code block 并注明目标路径
- 无问题则明确写「无明显可改项」，勿为改而改

## 风险分级

| 级别 | 含义 |
|------|------|
| P0 阻断 | 安全漏洞、数据错误、规范硬禁止项、接口必炸不一致 |
| P1 高 | 明确 bug、错误 HTTP/异常语义、鉴权遗漏、明显性能坑 |
| P2 中 | 边界缺失、可维护性/规范偏离、潜在不一致 |
| P3 低 | 可选润色；与「最小改动」冲突时降级或不报 |

## 输出模板

按此结构输出（无某类问题时写「无」）：

```markdown
# 代码审查报告

## 范围
- 文件与行号：…
- 规范依据：`.cursorrules`

## 汇总
- P0: n | P1: n | P2: n | P3: n
- 一句话结论：…

## 问题列表

### [P0|P1|P2|P3] 简短标题
- 文件：`path/to/File.ext`（Lx–Ly）
- 维度：规范 | Bug/边界/性能 | 接口一致性
- 问题：…
- 依据：`.cursorrules` 相关条款（简述）
- 建议：…

## 优化代码
### `path/to/File.ext`
（最小可粘贴补丁；无则写「无」）

## 接口一致性（如适用）
| 项 | 后端 | 前端 | 结论 |
|----|------|------|------|
| … | … | … | 一致 / 不一致 |
```

## 约束

- 只审与范围相关的问题；不做无关重构或跨模块「统一化」建议。
- 不提交密钥；不写 exploit / 攻击性 PoC。
- 用户未要求时不直接改代码；默认先给报告与优化示例，问是否落地修改。
- 规范冲突时以 `.cursorrules` 与同模块邻近文件为准。
