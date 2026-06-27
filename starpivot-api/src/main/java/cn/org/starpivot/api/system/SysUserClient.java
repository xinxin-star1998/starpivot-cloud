package cn.org.starpivot.api.system;

import cn.org.starpivot.api.system.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统用户 Feign 客户端接口。
 * <p>
 * 供 auth 等服务直连 {@code starpivot-system} 微服务（不经网关），
 * 调用用户鉴权、菜单查询、注册等内部接口。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link FeignClient} — 声明 Feign 客户端，{@code name} 指定目标服务名，{@code contextId} 避免同服务多 Client Bean 冲突</li>
 * </ul>
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysUserClient",
        path = "/api/${starpivot.api.version:v1}")
public interface SysUserClient {

    /**
     * 按用户名查询用户鉴权信息（不含密码）。
     *
     * @param username 用户名
     * @return 用户鉴权 DTO，含角色、状态等
     */
    @GetMapping("/internal/user/username/{username}")
    Result<SysUserAuthDto> getByUsername(@PathVariable("username") String username);

    /**
     * 校验用户名与密码，成功时返回用户鉴权信息。
     *
     * @param request 含用户名与明文密码的校验请求
     * @return 校验通过后的用户鉴权 DTO
     */
    @PostMapping("/internal/user/verify-password")
    Result<SysUserAuthDto> verifyPassword(@RequestBody VerifyPasswordRequest request);

    /**
     * 查询指定用户的菜单/权限树。
     *
     * @param userId 用户 ID
     * @return 菜单 DTO 列表
     */
    @GetMapping("/internal/user/{userId}/menus")
    Result<List<SysMenuDto>> getUserMenus(@PathVariable("userId") Long userId);

    /**
     * 内部用户注册（由 auth 服务调用）。
     *
     * @param request 注册用户名与密码
     * @return 注册成功后的用户基本信息
     */
    @PostMapping("/internal/user/register")
    Result<RegisterUserResponse> registerUser(@RequestBody RegisterUserRequest request);

    @PostMapping("/internal/user/forgot-password")
    Result<Boolean> resetPasswordByForgot(@RequestBody ForgotPasswordResetRequest request);
}
