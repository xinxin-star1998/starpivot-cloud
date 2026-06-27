package cn.org.starpivot.api.system;

import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 系统配置 Feign 客户端接口。
 * <p>
 * 供 auth 等服务查询系统级开关配置（如是否开放注册）。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link FeignClient} — 声明 Feign 客户端，直连 {@code starpivot-system} 服务</li>
 * </ul>
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysConfigClient",
        path = "/api/${starpivot.api.version:v1}")
public interface SysConfigClient {

    /**
     * 查询系统是否开放用户自助注册。
     *
     * @return {@code true} 表示已开放注册
     */
    @GetMapping("/internal/config/register-enabled")
    Result<Boolean> isRegisterEnabled();

    @GetMapping("/internal/config/forget-password-enabled")
    Result<Boolean> isForgetPasswordEnabled();
}
