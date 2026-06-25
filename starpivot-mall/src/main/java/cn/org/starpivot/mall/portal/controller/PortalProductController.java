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
import org.springframework.web.bind.annotation.*;

/**
 * C端-商品控制器。
 * <p>
 * 商品检索与详情（仅上架商品）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/product}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-商品」</li>
 * </ul>
 *
 * @see PortalProductService
 */

@RestController
@RequestMapping("/portal/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-商品", description = "商品检索与详情（仅上架商品）")
public class PortalProductController {

    private final PortalProductService portalProductService;

    /**
     * 商品搜索/列表。
     *
     * @param bo 业务请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "商品搜索/列表")
    @PostMapping("/search")
    public Result<PageResponse<PortalProductListVo>> search(@RequestBody PortalProductSearchBo bo) {
        return Result.success(portalProductService.search(bo));
    }

    /**
     * 商品详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<PortalProductDetailVo> detail(@PathVariable("id") Long id) {
        return Result.success(portalProductService.getDetail(id));
    }
}
