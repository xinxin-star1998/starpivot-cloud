package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.domain.vo.PortalHomeVo;
import cn.org.starpivot.mall.portal.service.PortalHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/home")
@RequiredArgsConstructor
@Tag(name = "C端-首页", description = "轮播、分类树等公开数据")
public class PortalHomeController {

    private final PortalHomeService portalHomeService;

    @Operation(summary = "首页聚合数据")
    @GetMapping
    public Result<PortalHomeVo> home() {
        return Result.success(portalHomeService.getHomeData());
    }
}
