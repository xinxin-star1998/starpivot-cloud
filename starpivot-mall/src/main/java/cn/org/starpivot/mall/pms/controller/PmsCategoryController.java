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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/category")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-分类")
public class PmsCategoryController {

    private final PmsCategoryService pmsCategoryService;

    @Operation(summary = "分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<List<CategoryTreeVo>> tree() {
        return Result.success(pmsCategoryService.treeList());
    }

    @Operation(summary = "子分类列表")
    @GetMapping("/children")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<List<CategoryTreeVo>> children(
            @RequestParam(value = "parentCid", required = false) Long parentCid) {
        return Result.success(pmsCategoryService.listChildren(parentCid));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:category:query')")
    public Result<CategoryTreeVo> getById(@PathVariable("id") Long id) {
        return Result.success(pmsCategoryService.getDetail(id));
    }

    @Log(title = "新增商品分类", businessType = BusinessType.INSERT)
    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:category:add')")
    public Result<?> add(@Valid @RequestBody CategorySaveBo bo) {
        pmsCategoryService.addCategory(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改商品分类", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改分类")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:category:edit')")
    public Result<?> update(@Valid @RequestBody CategorySaveBo bo) {
        pmsCategoryService.updateCategory(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除商品分类", businessType = BusinessType.DELETE)
    @Operation(summary = "删除分类")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:category:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        pmsCategoryService.removeCategories(ids);
        return Result.success("删除成功");
    }

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
