package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundAlertSummaryVo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.service.OmsRefundAlertService;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
import cn.org.starpivot.mall.oms.service.OmsRefundProcessService;
import cn.org.starpivot.mall.oms.service.OmsRefundSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商城-退款流水控制器。
 * <p>
 * 退款流水只读查询。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/refund}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-退款流水」</li>
 * </ul>
 *
 * @see OmsRefundInfoService
 */

@RestController
@RequestMapping("/mall/refund")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-退款流水", description = "退款流水只读查询")
public class OmsRefundController {

    private final OmsRefundInfoService omsRefundInfoService;
    private final OmsRefundSyncService omsRefundSyncService;
    private final OmsRefundProcessService omsRefundProcessService;
    private final OmsRefundAlertService omsRefundAlertService;

    /**
     * 退款流水分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "退款流水分页列表")
    @PostMapping("/refundPageList")
    @PreAuthorize("hasAnyAuthority('mall:refund:list', 'mall:refund:query')")
    public Result<PageResponse<RefundVo>> pageList(@RequestBody RefundReqBo reqBo) {
        return Result.success(omsRefundInfoService.pageList(reqBo));
    }

    @Operation(summary = "退款流水详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:refund:query')")
    public Result<RefundVo> getById(@PathVariable("id") Long id) {
        return Result.success(omsRefundInfoService.getDetailById(id));
    }

    @Operation(summary = "同步退款状态（向支付渠道查询）")
    @PutMapping("/{id}/sync")
    @PreAuthorize("hasAnyAuthority('mall:refund:query', 'mall:refund:edit')")
    public Result<RefundVo> syncStatus(@PathVariable("id") Long id) {
        return Result.success(omsRefundSyncService.syncStatus(id));
    }

    @Operation(summary = "重试原路退款（失败/待退款）")
    @PutMapping("/{id}/retry")
    @PreAuthorize("hasAnyAuthority('mall:refund:query', 'mall:refund:edit')")
    public Result<RefundVo> retryRefund(@PathVariable("id") Long id) {
        return Result.success(omsRefundProcessService.retryFailedRefund(id));
    }

    @Operation(summary = "退款失败告警摘要")
    @GetMapping("/alert/summary")
    @PreAuthorize("hasAuthority('mall:refund:query')")
    public Result<RefundAlertSummaryVo> alertSummary() {
        return Result.success(omsRefundAlertService.summary());
    }

    @Operation(summary = "确认退款失败告警已读")
    @PostMapping("/{id}/ack-alert")
    @PreAuthorize("hasAnyAuthority('mall:refund:query', 'mall:refund:edit')")
    public Result<?> acknowledgeAlert(@PathVariable("id") Long id) {
        omsRefundAlertService.acknowledge(id);
        return Result.success("已标记已读");
    }
}
