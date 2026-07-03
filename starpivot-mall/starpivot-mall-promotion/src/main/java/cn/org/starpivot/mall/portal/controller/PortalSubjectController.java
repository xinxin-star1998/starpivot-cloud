package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.domain.vo.PortalSubjectDetailVo;
import cn.org.starpivot.mall.portal.service.PortalSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * C 端专题活动控制器。
 */
@RestController
@RequestMapping("/portal/subject")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-专题活动", description = "首页专题落地页")
public class PortalSubjectController {

    private final PortalSubjectService portalSubjectService;

    @Operation(summary = "专题详情")
    @GetMapping("/{id}")
    public Result<PortalSubjectDetailVo> detail(
            @PathVariable("id") Long id,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "16") int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 48));
        return Result.success(portalSubjectService.getDetail(id, safePageNum, safePageSize));
    }
}
