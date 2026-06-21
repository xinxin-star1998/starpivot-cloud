package cn.org.starpivot.api.system;

import cn.org.starpivot.api.system.dto.RegisterUserRequest;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.api.system.dto.SysMenuDto;
import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统用户 Feign 客户端（直连 starpivot-system，不经网关）
 */
@FeignClient(name = "starpivot-system", contextId = "sysUserClient")
public interface SysUserClient {

    @GetMapping("/api/v1/internal/user/username/{username}")
    Result<SysUserAuthDto> getByUsername(@PathVariable("username") String username);

    @GetMapping("/api/v1/internal/user/{userId}/menus")
    Result<List<SysMenuDto>> getUserMenus(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/internal/user/register")
    Result<RegisterUserResponse> registerUser(@RequestBody RegisterUserRequest request);
}
