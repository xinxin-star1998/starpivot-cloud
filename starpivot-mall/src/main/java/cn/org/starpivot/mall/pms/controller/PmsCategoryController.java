package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.CategorySaveBo;
import cn.org.starpivot.mall.pms.domain.bo.CategorySortBatchBo;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.pms.service.PmsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-分类控制器。
 * <p>
 * 提供商城-分类相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/category}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-分类」</li>
 * </ul>
 *
 * @see PmsCategoryService
 */

@RestController
@RequestMapping("/mall/category")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-分类")
public class PmsCategoryController {

    private final PmsCategoryService pmsCategoryService;

    /**
     * 分类树。
     * @return 列表数据
     */
    @Operation(summary = "分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<List<CategoryTreeVo>> tree() {
        return Result.success(pmsCategoryService.treeList());
    }

    /**
     * 子分类列表。
     *
     * @param parentCid 主键 ID
     * @return 列表数据
     */
    @Operation(summary = "子分类列表")
    @GetMapping("/children")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<List<CategoryTreeVo>> children(
            @RequestParam(value = "parentCid", required = false) Long parentCid) {
        return Result.success(pmsCategoryService.listChildren(parentCid));
    }

    /**
     * 分类详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<CategoryTreeVo> getById(@PathVariable("id") Long id) {
        return Result.success(pmsCategoryService.getDetail(id));
    }

    /**
     * 新增分类。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增商品分类", businessType = BusinessType.INSERT)
    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:category:add')")
    public Result<?> add(@Valid @RequestBody CategorySaveBo bo) {
        pmsCategoryService.addCategory(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改分类。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改商品分类", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改分类")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:category:edit')")
    public Result<?> update(@Valid @RequestBody CategorySaveBo bo) {
        pmsCategoryService.updateCategory(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除分类。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除商品分类", businessType = BusinessType.DELETE)
    @Operation(summary = "删除分类")
    @DeleteMapping("/removeCategory")
    @PreAuthorize("hasAuthority('mall:category:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        pmsCategoryService.removeCategories(ids);
        return Result.success("删除成功");
    }

    /**
     * 批量更新同级排序。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "更新商品分类排序", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量更新同级排序")
    @PutMapping("/sort")
    @PreAuthorize("hasAuthority('mall:category:edit')")
    public Result<?> updateSort(@Valid @RequestBody CategorySortBatchBo bo) {
        pmsCategoryService.updateSortBatch(bo.getItems());
        return Result.success("排序已更新");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
