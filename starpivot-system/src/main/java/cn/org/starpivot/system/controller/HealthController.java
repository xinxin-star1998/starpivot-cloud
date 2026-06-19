package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "健康检查")
@RestController
@RequestMapping("/health")
public class HealthController {

    @Operation(summary = "服务存活探测")
    @GetMapping
    public Result<Map<String, String>> health() {
        return Result.success(Map.of(
                "service", "starpivot-system",
                "status", "UP"
        ));
    }
}
