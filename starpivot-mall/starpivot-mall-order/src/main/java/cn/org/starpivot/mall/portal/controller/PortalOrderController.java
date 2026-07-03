package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderPriceTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceTrialVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitTokenVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C端-订单控制器。
 * <p>
 * 提交订单、我的订单。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/order}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-订单」</li>
 * </ul>
 *
 * @see PortalOrderService
 */

@RestController
@RequestMapping("/portal/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-订单", description = "提交订单、我的订单")
public class PortalOrderController {

    private final PortalOrderService portalOrderService;

    @Operation(summary = "获取下单防重令牌")
    @GetMapping("/submit-token")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderSubmitTokenVo> submitToken() {
        return Result.success(portalOrderService.issueSubmitToken(PortalMemberContext.requireMemberId()));
    }

    @Operation(summary = "结算价格试算")
    @PostMapping("/price-trial")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderPriceTrialVo> priceTrial(@RequestBody PortalOrderPriceTrialBo bo) {
        return Result.success(portalOrderService.priceTrial(PortalMemberContext.requireMemberId(), bo));
    }

    /**
     * 提交订单。
     *
     * @param bo 业务请求参数
     * @return 业务数据
     */
    @Operation(summary = "提交订单")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderSubmitVo> submit(@Valid @RequestBody PortalOrderSubmitBo bo) {
        return Result.success(portalOrderService.submit(PortalMemberContext.requireMemberId(), bo));
    }

    /**
     * 我的订单分页。
     *
     * @param bo 业务请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "我的订单分页")
    @PostMapping("/portalOrderPageList")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PageResponse<PortalOrderVo>> pageList(@RequestBody PortalOrderQueryBo bo) {
        return Result.success(portalOrderService.pageMyOrders(PortalMemberContext.requireMemberId(), bo));
    }

    /**
     * 订单详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderVo> detail(@PathVariable("id") Long id) {
        return Result.success(portalOrderService.getMyOrder(PortalMemberContext.requireMemberId(), id));
    }

    /**
     * 取消订单。
     *
     * @param id 主键 ID
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> cancel(@PathVariable("id") Long id) {
        portalOrderService.cancel(PortalMemberContext.requireMemberId(), id);
        return Result.success("取消成功");
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> confirmReceive(@PathVariable("id") Long id) {
        portalOrderService.confirmReceive(PortalMemberContext.requireMemberId(), id);
        return Result.success("确认收货成功");
    }

    @Operation(summary = "申请退货")
    @PostMapping("/return")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<Long>> applyReturn(@Valid @RequestBody cn.org.starpivot.mall.portal.domain.bo.PortalOrderReturnApplyBo bo) {
        return Result.success(portalOrderService.applyReturn(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "订单状态数量")
    @GetMapping("/status-counts")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<Map<String, Integer>> statusCounts() {
        return Result.success(portalOrderService.statusCounts(PortalMemberContext.requireMemberId()));
    }
}
