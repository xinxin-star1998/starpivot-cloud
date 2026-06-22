package cn.org.starpivot.api.system;

import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * 操作日志 Feign 客户端接口。
 * <p>
 * 供 monitor 定时任务等服务调用，清理 system 模块中的操作日志。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link FeignClient} — 声明 Feign 客户端，直连 {@code starpivot-system} 服务</li>
 * </ul>
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysOperLogClient",
        path = "/api/${starpivot.api.version:v1}")
public interface SysOperLogClient {

    /**
     * 清空全部操作日志（通常由定时任务调用）。
     *
     * @return 操作结果
     */
    @DeleteMapping("/internal/operlog/clean")
    Result<Void> cleanAll();
}
