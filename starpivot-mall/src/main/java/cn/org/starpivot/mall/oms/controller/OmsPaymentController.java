package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.PaymentReqBo;
import cn.org.starpivot.mall.oms.domain.vo.PaymentVo;
import cn.org.starpivot.mall.oms.service.OmsPaymentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商城-支付流水控制器。
 * <p>
 * 支付流水只读查询。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/payment}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-支付流水」</li>
 * </ul>
 *
 * @see OmsPaymentInfoService
 */

@RestController
@RequestMapping("/mall/payment")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-支付流水", description = "支付流水只读查询")
public class OmsPaymentController {

    private final OmsPaymentInfoService omsPaymentInfoService;

    /**
     * 支付流水分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "支付流水分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAnyAuthority('mall:payment:list', 'mall:payment:query')")
    public Result<PageResponse<PaymentVo>> pageList(@RequestBody PaymentReqBo reqBo) {
        return Result.success(omsPaymentInfoService.pageList(reqBo));
    }
}
