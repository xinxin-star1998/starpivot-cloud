package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.pms.support.MallImageDisplaySupport;
import cn.org.starpivot.mall.portal.domain.vo.PortalHomeVo;
import cn.org.starpivot.mall.portal.service.PortalHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C端-首页控制器。
 * <p>
 * 轮播、分类树等公开数据。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/home}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-首页」</li>
 * </ul>
 *
 * @see PortalHomeService
 */

@RestController
@RequestMapping("/portal/home")
@RequiredArgsConstructor
@Tag(name = "C端-首页", description = "轮播、分类树等公开数据")
public class PortalHomeController {

    private final PortalHomeService portalHomeService;
    private final MallImageDisplaySupport mallImageDisplaySupport;

    /**
     * 首页聚合数据。
     * @return 业务数据
     */
    @Operation(summary = "首页聚合数据")
    @GetMapping
    public Result<PortalHomeVo> home() {
        return Result.success(portalHomeService.getHomeData());
    }

    @Operation(summary = "批量获取商城展示图 URL（公开）")
    @PostMapping("/presigned-urls")
    public Result<Map<String, String>> presignedUrls(@RequestBody List<String> objectNames) {
        return Result.success(mallImageDisplaySupport.resolveDisplayUrls(objectNames));
    }
}
