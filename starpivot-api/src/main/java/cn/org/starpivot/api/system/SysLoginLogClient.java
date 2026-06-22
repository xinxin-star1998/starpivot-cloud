package cn.org.starpivot.api.system;

import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 登录日志 Feign 客户端接口。
 * <p>
 * 供 auth 服务异步记录用户登录/登出行为到 system 模块。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link FeignClient} — 声明 Feign 客户端，直连 {@code starpivot-system} 服务</li>
 * </ul>
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysLoginLogClient",
        path = "/api/${starpivot.api.version:v1}")
public interface SysLoginLogClient {

    /**
     * 保存一条登录日志记录。
     *
     * @param loginLogDto 登录日志数据（用户名、IP、浏览器、结果等）
     * @return 操作结果
     */
    @PostMapping("/internal/logininfor")
    Result<Void> saveLoginLog(@RequestBody LoginLogDto loginLogDto);
}
