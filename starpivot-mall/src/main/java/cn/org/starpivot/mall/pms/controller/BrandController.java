package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.BrandCategoryBindBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandReqBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.BrandCategoryVo;
import cn.org.starpivot.mall.pms.domain.vo.BrandVo;
import cn.org.starpivot.mall.pms.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-品牌控制器。
 * <p>
 * 提供商城-品牌相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/brand}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-品牌」</li>
 * </ul>
 *
 * @see BrandService
 */

@RestController
@RequestMapping("/mall/brand")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-品牌")
public class BrandController {

    private final BrandService brandService;

    /**
     * 品牌分页列表。
     *
     * @param brandReqBo 业务请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "品牌分页列表")
    @PostMapping("/brandPageList")
    @PreAuthorize("hasAuthority('mall:brand:query')")
    public Result<PageResponse<BrandVo>> pageList(@RequestBody BrandReqBo brandReqBo) {
        return Result.success(brandService.pageList(brandReqBo));
    }

    /**
     * 品牌详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "品牌详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:brand:query')")
    public Result<BrandVo> getById(@PathVariable("id") Long id) {
        return Result.success(brandService.getById(id));
    }

    /**
     * 新增品牌。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增品牌", businessType = BusinessType.INSERT)
    @Operation(summary = "新增品牌")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:brand:add')")
    public Result<?> add(@Valid @RequestBody BrandSaveBo bo) {
        brandService.addBrand(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改品牌。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改品牌", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改品牌")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:brand:edit')")
    public Result<?> update(@Valid @RequestBody BrandSaveBo bo) {
        brandService.updateBrand(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除品牌。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除品牌", businessType = BusinessType.DELETE)
    @Operation(summary = "删除品牌")
    @DeleteMapping("/removeBrand")
    @PreAuthorize("hasAuthority('mall:brand:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        brandService.removeByIds(ids);
        return Result.success("删除成功");
    }

    /**
     * 品牌已绑定的三级分类列表。
     *
     * @param id 主键 ID
     * @return 列表数据
     */
    @Operation(summary = "品牌已绑定的三级分类列表")
    @GetMapping("/{id}/categories")
    @PreAuthorize("hasAuthority('mall:brand:query')")
    public Result<List<BrandCategoryVo>> listBoundCategories(@PathVariable("id") Long id) {
        return Result.success(brandService.listBoundCategories(id));
    }

    /**
     * 品牌绑定三级分类。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "品牌绑定分类", businessType = BusinessType.UPDATE)
    @Operation(summary = "品牌绑定三级分类")
    @PutMapping("/categories")
    @PreAuthorize("hasAuthority('mall:brand:edit')")
    public Result<?> bindCategories(@Valid @RequestBody BrandCategoryBindBo bo) {
        brandService.bindCategories(bo);
        return Result.success("绑定成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
