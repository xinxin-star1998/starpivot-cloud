package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApplyService;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商城-退货控制器。
 * <p>
 * 退货申请查询与审核。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/order-return}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-退货」</li>
 * </ul>
 *
 * @see OmsOrderReturnApplyService
 */

@RestController
@RequestMapping("/mall/order-return")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-退货", description = "退货申请查询与审核")
public class OmsOrderReturnController {

    private final OmsOrderReturnApplyService omsOrderReturnApplyService;
    private final OmsOrderReturnApprovalService omsOrderReturnApprovalService;

    /**
     * 退货申请分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "退货申请分页列表")
    @PostMapping("/orderReturnPageList")
    @PreAuthorize("hasAuthority('mall:return:query')")
    public Result<PageResponse<ReturnVo>> pageList(@RequestBody ReturnReqBo reqBo) {
        return Result.success(omsOrderReturnApplyService.pageList(reqBo));
    }

    /**
     * 退货申请详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "退货申请详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:return:query')")
    public Result<ReturnVo> getById(@PathVariable("id") Long id) {
        return Result.success(omsOrderReturnApplyService.getDetailById(id));
    }

    @Log(title = "提交退货审批", businessType = BusinessType.UPDATE)
    @Operation(summary = "提交退货申请审批")
    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("hasAuthority('mall:return:audit')")
    public Result<?> submitApproval(@PathVariable("id") Long id) {
        omsOrderReturnApprovalService.submitApproval(id);
        return Result.success("已提交审批");
    }

    @Log(title = "完成退货", businessType = BusinessType.UPDATE)
    @Operation(summary = "完成退货（入库+退款记录）")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('mall:return:audit')")
    public Result<?> complete(@PathVariable("id") Long id) {
        omsOrderReturnApplyService.completeReturn(id);
        return Result.success("退货已完成");
    }
}
