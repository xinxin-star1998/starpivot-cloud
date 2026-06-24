package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.SkuCreateBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPriceBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPublishStatusBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuReqBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;
import cn.org.starpivot.mall.pms.service.PmsSkuInfoService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/sku")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SKU", description = "SKU 查询与维护（表 pms_sku_info）")
public class PmsSkuInfoController {

    private final PmsSkuInfoService pmsSkuInfoService;

    @Operation(summary = "SKU 分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<PageResponse<SkuVo>> pageList(@RequestBody SkuReqBo reqBo) {
        return Result.success(pmsSkuInfoService.getSkuPageList(reqBo));
    }

    @Operation(summary = "SKU 详情")
    @GetMapping("/{skuId}")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<SkuVo> getById(@PathVariable("skuId") Long skuId) {
        return Result.success(pmsSkuInfoService.getSkuById(skuId));
    }

    @Log(title = "新增SKU", businessType = BusinessType.INSERT)
    @Operation(summary = "新增 SKU", description = "挂载已有 SPU，写入 pms_sku_info 及销售属性/图片子表")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:product:add')")
    public Result<Long> add(@Valid @RequestBody SkuCreateBo bo) {
        return Result.success(pmsSkuInfoService.createSku(bo));
    }

    @Log(title = "修改SKU", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改 SKU")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> update(@Valid @RequestBody SkuSaveBo bo) {
        pmsSkuInfoService.updateSku(bo);
        return Result.success("修改成功");
    }

    @Log(title = "SKU改价", businessType = BusinessType.UPDATE)
    @Operation(summary = "SKU 改价")
    @PutMapping("/price")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> updatePrice(@Valid @RequestBody SkuPriceBo bo) {
        pmsSkuInfoService.updatePrice(bo);
        return Result.success("改价成功");
    }

    @Log(title = "SKU上下架", businessType = BusinessType.UPDATE)
    @Operation(summary = "SKU 上下架", description = "更新所属 SPU 的 publishStatus：0-下架 1-上架")
    @PutMapping("/publish-status")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> updatePublishStatus(@Valid @RequestBody SkuPublishStatusBo bo) {
        pmsSkuInfoService.updatePublishStatus(bo);
        return Result.success(bo.getPublishStatus() == 1 ? "上架成功" : "下架成功");
    }

    @Log(title = "删除SKU", businessType = BusinessType.DELETE)
    @Operation(summary = "删除 SKU", description = "请求体 ids 为 SKU 主键列表")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        pmsSkuInfoService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
