package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.support.MallImageDisplaySupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;
import cn.org.starpivot.mall.portal.service.PortalProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-商品", description = "商品检索与详情（仅上架商品）")
public class PortalProductController {

    private final PortalProductService portalProductService;
    private final MallImageDisplaySupport mallImageDisplaySupport;

    @Operation(summary = "商品搜索/列表")
    @PostMapping("/search")
    public Result<PageResponse<PortalProductListVo>> search(@RequestBody PortalProductSearchBo bo) {
        return Result.success(portalProductService.search(bo));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<PortalProductDetailVo> detail(@PathVariable("id") Long id) {
        return Result.success(portalProductService.getDetail(id));
    }

    @Operation(summary = "相关推荐")
    @GetMapping("/{id}/related")
    public Result<List<PortalProductListVo>> related(
            @PathVariable("id") Long id,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return Result.success(portalProductService.listRelated(id, safeLimit));
    }

    @Operation(summary = "批量获取商品展示图 URL（公开）")
    @PostMapping("/presigned-urls")
    public Result<Map<String, String>> presignedUrls(@RequestBody List<String> objectNames) {
        return Result.success(mallImageDisplaySupport.resolveDisplayUrls(objectNames));
    }
}
