package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.oms.domain.bo.ReturnAuditBo;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 退货处理
 */
@RestController
@RequestMapping("/mall/order-return")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-退货", description = "退货申请查询与审核")
public class OmsOrderReturnController {

    private final OmsOrderReturnApplyService omsOrderReturnApplyService;

    @Operation(summary = "退货申请分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:return:query')")
    public Result<PageResponse<ReturnVo>> pageList(@RequestBody ReturnReqBo reqBo) {
        return Result.success(omsOrderReturnApplyService.pageList(reqBo));
    }

    @Operation(summary = "退货申请详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:return:query')")
    public Result<ReturnVo> getById(@PathVariable("id") Long id) {
        return Result.success(omsOrderReturnApplyService.getDetailById(id));
    }

    @Log(title = "退货审核", businessType = BusinessType.UPDATE)
    @Operation(summary = "退货审核")
    @PutMapping("/audit")
    @PreAuthorize("hasAuthority('mall:return:audit')")
    public Result<?> audit(@Valid @RequestBody ReturnAuditBo bo) {
        omsOrderReturnApplyService.audit(bo);
        return Result.success("审核成功");
    }
}
