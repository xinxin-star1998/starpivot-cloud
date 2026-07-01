package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalSeckillOrderBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalSeckillPageVo;
import cn.org.starpivot.mall.portal.service.PortalSeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端秒杀页控制器。
 */
@RestController
@RequestMapping("/portal/seckill")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-秒杀", description = "限时秒杀场次与商品")
public class PortalSeckillController {

    private final PortalSeckillService portalSeckillService;

    @Operation(summary = "秒杀页数据")
    @GetMapping
    public Result<PortalSeckillPageVo> page(@RequestParam(value = "sessionId", required = false) Long sessionId) {
        return Result.success(portalSeckillService.getPage(sessionId));
    }

    @Operation(summary = "秒杀下单")
    @PostMapping("/order")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderSubmitVo> placeOrder(@Valid @RequestBody PortalSeckillOrderBo bo) {
        return Result.success(portalSeckillService.placeOrder(PortalMemberContext.requireMemberId(), bo));
    }
}
