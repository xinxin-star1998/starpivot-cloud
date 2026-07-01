package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.*;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;
import cn.org.starpivot.mall.pms.service.PmsSkuInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-SKU控制器。
 * <p>
 * SKU 查询与维护（表 pms_sku_info）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/sku}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-SKU」</li>
 * </ul>
 *
 * @see PmsSkuInfoService
 */

@RestController
@RequestMapping("/mall/sku")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SKU", description = "SKU 查询与维护（表 pms_sku_info）")
public class PmsSkuInfoController {

    private final PmsSkuInfoService pmsSkuInfoService;

    /**
     * SKU 分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "SKU 分页列表")
    @PostMapping("/skuPageList")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<PageResponse<SkuVo>> pageList(@RequestBody SkuReqBo reqBo) {
        return Result.success(pmsSkuInfoService.getSkuPageList(reqBo));
    }

    /**
     * SKU 详情。
     *
     * @param skuId 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "SKU 详情")
    @GetMapping("/{skuId}")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<SkuVo> getById(@PathVariable("skuId") Long skuId) {
        return Result.success(pmsSkuInfoService.getSkuById(skuId));
    }

    /**
     * 新增 SKU。
     *
     * @param bo 业务请求参数
     * @return 业务数据
     */
    @Log(title = "新增SKU", businessType = BusinessType.INSERT)
    @Operation(summary = "新增 SKU", description = "挂载已有 SPU，写入 pms_sku_info 及销售属性/图片子表")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:product:add')")
    public Result<Long> add(@Valid @RequestBody SkuCreateBo bo) {
        return Result.success(pmsSkuInfoService.createSku(bo));
    }

    /**
     * 修改 SKU。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改SKU", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改 SKU")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> update(@Valid @RequestBody SkuSaveBo bo) {
        pmsSkuInfoService.updateSku(bo);
        return Result.success("修改成功");
    }

    /**
     * SKU 改价。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "SKU改价", businessType = BusinessType.UPDATE)
    @Operation(summary = "SKU 改价")
    @PutMapping("/price")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> updatePrice(@Valid @RequestBody SkuPriceBo bo) {
        pmsSkuInfoService.updatePrice(bo);
        return Result.success("改价成功");
    }

    /**
     * SKU 上下架。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "SKU上下架", businessType = BusinessType.UPDATE)
    @Operation(summary = "SKU 上下架", description = "更新所属 SPU 的 publishStatus：0-下架 1-上架")
    @PutMapping("/publish-status")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> updatePublishStatus(@Valid @RequestBody SkuPublishStatusBo bo) {
        pmsSkuInfoService.updatePublishStatus(bo);
        return Result.success(bo.getPublishStatus() == 1 ? "上架成功" : "下架成功");
    }

    /**
     * 删除 SKU。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除SKU", businessType = BusinessType.DELETE)
    @Operation(summary = "删除 SKU", description = "请求体 ids 为 SKU 主键列表")
    @DeleteMapping("/removeSku")
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
