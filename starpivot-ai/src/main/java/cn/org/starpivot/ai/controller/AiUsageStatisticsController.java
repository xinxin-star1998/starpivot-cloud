package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiUsageLogQueryDto;
import cn.org.starpivot.ai.domain.vo.AiUsageLogVo;
import cn.org.starpivot.ai.domain.vo.AiUsageSummaryVo;
import cn.org.starpivot.ai.service.AiUsageLogService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/statistics")
@RequiredArgsConstructor
@Tag(name = "AI-用量统计", description = "AI 调用用量统计")
public class AiUsageStatisticsController {

    private final AiUsageLogService aiUsageLogService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ai:statistics:query')")
    @Operation(summary = "用量汇总")
    public Result<AiUsageSummaryVo> summary(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(aiUsageLogService.summary(beginTime, endTime));
    }

    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:statistics:query')")
    @Operation(summary = "用量日志分页")
    public Result<PageResponse<AiUsageLogVo>> pageList(@RequestBody AiUsageLogQueryDto query) {
        return Result.success(aiUsageLogService.pageList(query));
    }
}
