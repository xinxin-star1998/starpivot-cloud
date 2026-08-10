package cn.org.starpivot.mall.portal.subscribe.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.subscribe.domain.vo.PortalSubscribeTemplatesVo;
import cn.org.starpivot.mall.portal.subscribe.service.PortalMiniSubscribeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/subscribe")
@RequiredArgsConstructor
@Tag(name = "C端-订阅消息", description = "小程序订阅消息模板")
public class PortalSubscribeController {

    private final PortalMiniSubscribeService portalMiniSubscribeService;

    @Operation(summary = "可申请的订阅消息模板")
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalSubscribeTemplatesVo> templates() {
        return Result.success(portalMiniSubscribeService.templates());
    }
}
