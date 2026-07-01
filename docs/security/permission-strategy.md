# 权限策略说明

StarPivot 微服务采用 **Gateway JWT 校验 + 下游 MicroserviceAuthenticationFilter + Spring `@PreAuthorize`** 的分层鉴权模型。  
各服务在解析 JWT / 网关透传头后，通过 `AuthorityResolver` 将用户角色与权限标识写入 `SecurityContext`，供方法级鉴权使用。

## 鉴权链路

```
Client (Bearer JWT)
  → Gateway (AuthGlobalFilter: 校验 JWT、黑名单、注入 X-User-* 头)
  → 微服务 (MicroserviceAuthenticationFilter: 解析 Token/头、加载 Authorities)
  → Controller (@PreAuthorize)
```

内部服务调用（`/internal/**`）不走 JWT，由 `InternalServiceAuthFilter` 校验 `X-Internal-Token`。

## 三种 Authority 策略

配置项：`starpivot.security.authority-strategy`（见 `MicroserviceSecurityProperties`）

| 策略值 | 说明 | 适用场景 |
|--------|------|----------|
| `roles-only`（默认） | 仅将 JWT/头中的 **角色** 写入 Authorities | 无细粒度菜单权限、或仅校验登录态的服务 |
| `menu-permission` | 从数据库加载 **菜单 + 按钮权限** 字符串 | 与 system 共用 RBAC 表、需 `hasAuthority('xxx:yyy:query')` 的服务 |
| `custom` | 业务模块自行注册 `AuthorityResolver` Bean | 权限来源特殊、需定制加载逻辑 |

> 当模块已提供自定义 `AuthorityResolver` Bean 时，自动配置不会覆盖（`@ConditionalOnMissingBean`）。

## 各服务当前策略

| 服务 | 策略 | 实现方式 |
|------|------|----------|
| **starpivot-system** | custom | `SystemAuthorityResolverConfiguration` → `AuthorityLoaderService`（角色 + 菜单/按钮权限，含超管缓存） |
| **starpivot-monitor** | menu-permission | `application.yml` 中 `starpivot.security.authority-strategy: menu-permission` |
| **starpivot-generator** | menu-permission | 同上 |
| **starpivot-auth** | roles-only | 默认；认证接口以白名单为主，业务接口较少 |
| **starpivot-file** | roles-only | 默认；上传接口主要校验登录态 |

## 新增模块如何选择

1. **仅需要登录、不校验按钮权限**（如纯工具、文件代理）  
   - 不配置或显式设置 `authority-strategy: roles-only`

2. **与 system 共用 `sys_menu` 权限体系**（监控、代码生成等同款后台模块）  
   - 设置 `authority-strategy: menu-permission`  
   - 确保模块依赖 `starpivot-common` 且能扫描到 `AuthMenuMapper`（需 MyBatis 与 system 库表同源）  
   - Controller 使用 `@PreAuthorize("hasAuthority('模块:资源:操作')")`

3. **权限来源独立**（多租户、外部 IAM 等）  
   - 在本模块新增 `@Configuration`，注册自定义 `AuthorityResolver` Bean  
   - 不要设置 `menu-permission`，以免与自定义 Bean 冲突

## Controller 注解规范

统一使用 Spring Security 标准注解（**不要**使用已废弃的自定义 `@RequiresPermissions`）。

列表与删除路径命名：`POST /{entity}PageList`、`DELETE /remove{Entity}`（详见根目录 [README.md](../../README.md) §API 路径命名约定）。

```java
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasAuthority('system:user:query')")
@PostMapping("/userPageList")
public Result<?> pageList(@RequestBody UserReqBo query) { ... }

@PreAuthorize("hasAuthority('system:user:delete')")
@DeleteMapping("/removeUser")
public Result<?> remove(@RequestBody DeleteRequest request) { ... }

@PreAuthorize("isAuthenticated()")
@GetMapping("/profile")
public Result<?> profile() { ... }
```

代码生成模板（`starpivot-generator/.../controller.java.vm`）已对齐上述 import。

## 权限标识约定

- 格式：`{模块}:{资源}:{操作}`，例如 `system:user:query`、`monitor:online:force-logout`
- 与 `sys_menu.perms` 字段及前端按钮 `v-auth` 保持一致
- 超级管理员：system 模块对 admin 用户加载全部权限字符串

## 相关代码位置

| 组件 | 路径 |
|------|------|
| 策略配置 | `starpivot-common/.../MicroserviceSecurityProperties.java` |
| 自动配置 | `starpivot-common/.../MicroserviceAuthenticationAutoConfiguration.java` |
| 网关 JWT | `starpivot-gateway/.../filter/AuthGlobalFilter.java` |
| 下游过滤器 | `starpivot-common/.../filter/MicroserviceAuthenticationFilter.java` |
| system 定制解析 | `starpivot-system/.../SystemAuthorityResolverConfiguration.java` |
