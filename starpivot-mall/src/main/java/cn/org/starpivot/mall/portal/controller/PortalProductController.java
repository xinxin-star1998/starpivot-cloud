package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;
import cn.org.starpivot.mall.portal.service.PortalProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-商品", description = "商品检索与详情（仅上架商品）")
public class PortalProductController {

    private final PortalProductService portalProductService;

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
}
