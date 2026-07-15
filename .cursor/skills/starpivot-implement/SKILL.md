---
name: starpivot-implement
description: >-
  Implement StarPivot Cloud features strictly per project-root .cursorrules:
  follow existing layering, reuse Result/BizException/http utils, no new tooling,
  produce code ready to integrate. Use when the user asks to 根据需求编写代码,
  按规范实现, 新增接口/页面, implement a feature, or write code that must follow
  .cursorrules and existing architecture.
---

# StarPivot 按规范实现

根据需求编写代码，严格遵守项目 `.cursorrules` 开发规范。
遵循现有项目架构分层，复用项目已有工具类、统一响应、异常处理、请求封装，不要自行引入新工具。
产出代码可以直接集成进当前项目。

## 何时启用

- 用户给出业务需求，要求实现前后端或其中一端
- 用户要求「按规范写」「可直接合入」「复用现有封装」
- 用户 `@starpivot-implement` 或点名本 skill

## 实施前必做

1. **读取** 仓库根目录 [`.cursorrules`](../../../.cursorrules)（完整阅读；不得凭记忆臆造规范）。
2. **定位同模块邻近实现**：打开同域现有 Controller / Service / API / 页面，按「就近一致」照抄模式，禁止套用外部模板另起风格。
3. **确认受众与路径**：管理端 `/mall|sys|approval|…`、C 端 `/portal`、服务间仅 `/internal`（前端永不调用 internal）。
4. 需求不清时先问边界；能从邻近代码推断的约定不要反复确认。

## 硬性约束

| 必须 | 禁止 |
|------|------|
| 后端 `Result` / `PageResponse` / `BizException` / `AssertUtils` | 新引入 AjaxResult、R、BusinessException、ServiceException |
| 前端 `@/utils/http` + `fetch*` API | 直接 axios；管理端/Portal Token 混用 |
| Controller 相对路径；Feign 契约在 `starpivot-api` | Controller 硬编码 `/api/v1`；业务服务内复制 Feign |
| 金额用 `@/utils/mall/money` | `toFixed` 做金额展示 |
| 校验/异常/OpenAPI 中文文案 | 明文密钥写入 Nacos/前端配置 |
| 最小改动、可编译、可合入 | 无关重构、跨模块「统一化」、新增 README（除非用户要求） |
| 写操作 POST/PUT/DELETE 不加重试 | 为风格统一做大范围重命名 |

## 实现工作流

```
进度:
- [ ] 1. 读 .cursorrules + 同模块范例
- [ ] 2. 定模块/分层/路径/权限串
- [ ] 3. 后端（如需要）
- [ ] 4. 前端（如需要）
- [ ] 5. 契约对齐自检
- [ ] 6. 交付说明（改了哪些文件）
```

### 后端（Java / Spring Cloud）

- 分层：`controller`（对外）/ `controller/internal`（Feign）→ `service`/`impl` → `mapper` + XML；领域对象放本模块 `domain`（VO/DTO 命名跟本模块主流）。
- 瘦 Controller：`@RestController` + `@RequiredArgsConstructor` + `@Validated`；体参 `@Valid`；`@PreAuthorize` / Portal 角色；OpenAPI 注解。
- 分页：入参 `PageReqBo`（`pageNum`/`pageSize`）；出参 `PageResponse`（`rows`/`total`…）。
- 事务：读 `readOnly`，写 `rollbackFor = Exception.class`，写在 ServiceImpl。
- 业务失败：抛 `BizException`；普通业务错误勿滥用 HTTP 500（见 `.cursorrules` GlobalExceptionHandler 约定）。
- 跨服务：Feign + FallbackFactory；关键消息走发件箱，禁止只 publish 当已可靠投递。
- 实体改前先看是否已有 `@TableLogic` / `BaseEntity`，勿想当然套用。

### 前端（star-pivot-ui）

- Vue 3 `<script setup lang="ts">` + 现有 `art-*` / Element Plus；禁止 Options API。
- API：`src/api/...` 中 `fetch*`；URL 与后端相对路径对齐；类型与 VO 同名同形（历史字段不一致以既有契约为准，禁止单端「纠正」）。
- 列表页优先 `useTable` + 既有搜索/表格组件结构；权限 `v-auth` 或 `useAuth().hasAuth`。
- Admin 用 `useUserStore`；Portal 用 `usePortalMemberStore` + `usePortalAuth`。

### 契约自检（改接口时必做）

- [ ] 路径受众正确（admin / portal / internal）
- [ ] `Result` / `PageResponse.rows` 前后端一致
- [ ] 权限串前后端同一套 `module:resource:action`
- [ ] 业务错误语义与 http 封装行为兼容

## 交付方式

- 直接改仓库内可合入文件；风格与同目录现有文件一致（import、注解、命名）。
- 完成后简短列出：新增/修改路径 + 如何自测（接口路径或页面入口）。
- 用户未要求时不 commit / 不 push / 不新建文档。

## 与审查 Skill 的分工

- 本 skill：按需求**实现**可合入代码。
- `starpivot-code-review`：对已有/选中代码做规范与风险审查。实现完成后若用户要求审查，再切审查 skill。
