package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponHistoryReqBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponHistoryVo;
import cn.org.starpivot.mall.sms.service.SmsCouponHistoryService;
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
 * 商城-优惠券发放记录控制器。
 * <p>
 * 提供商城-优惠券发放记录相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/coupon-history}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-优惠券发放记录」</li>
 * </ul>
 *
 * @see SmsCouponHistoryService
 */

@RestController
@RequestMapping("/mall/coupon-history")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-优惠券发放记录")
public class SmsCouponHistoryController {

    private final SmsCouponHistoryService smsCouponHistoryService;

    /**
     * 优惠券发放记录分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "优惠券发放记录分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:coupon:history')")
    public Result<PageResponse<CouponHistoryVo>> pageList(@RequestBody CouponHistoryReqBo reqBo) {
        return Result.success(smsCouponHistoryService.pageList(reqBo));
    }
}
