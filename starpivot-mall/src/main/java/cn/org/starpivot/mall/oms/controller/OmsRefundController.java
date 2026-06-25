package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
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

    /**
     * 退款流水分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "退款流水分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAnyAuthority('mall:refund:list', 'mall:refund:query')")
    public Result<PageResponse<RefundVo>> pageList(@RequestBody RefundReqBo reqBo) {
        return Result.success(omsRefundInfoService.pageList(reqBo));
    }
}
