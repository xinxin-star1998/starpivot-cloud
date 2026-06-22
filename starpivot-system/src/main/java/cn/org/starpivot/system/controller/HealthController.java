package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查控制器。
 * <p>
 * 提供无需鉴权的服务存活探测接口，供负载均衡与运维监控使用。
 * </p>
 * <ul>
 *   <li>{@link Tag} — OpenAPI 分组标签「健康检查」</li>
 *   <li>{@link RestController} — REST 风格控制器</li>
 *   <li>{@link RequestMapping} — 映射基础路径 {@code /health}</li>
 * </ul>
 */
@Tag(name = "健康检查")
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 返回服务名称与运行状态。
     *
     * @return 包含 {@code service} 与 {@code status=UP} 的成功响应
     */
    @Operation(summary = "服务存活探测")
    @GetMapping
    public Result<Map<String, String>> health() {
        return Result.success(Map.of(
                "service", "starpivot-system",
                "status", "UP"
        ));
    }
}
