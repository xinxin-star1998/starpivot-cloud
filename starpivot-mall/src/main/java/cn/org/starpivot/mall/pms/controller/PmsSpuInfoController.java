package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.bo.PublishStatusBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import cn.org.starpivot.mall.pms.service.PmsSpuInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mall/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SPU", description = "SPU CRUD（表 pms_spu_info）")
public class PmsSpuInfoController {

    private final PmsSpuInfoService pmsSpuInfoService;

    @Operation(summary = "SPU 分页列表", description = "支持 SPU 名称模糊，目录、品牌、上架状态筛选")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<PageResponse<ProductVo>> pageList(@RequestBody ProductReqBo productReqBo) {
        return Result.success(pmsSpuInfoService.getPmsSpuInfoPageList(productReqBo));
    }

    @Operation(summary = "SPU 详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public Result<ProductVo> getById(@PathVariable("id") Long id) {
        return Result.success(pmsSpuInfoService.getPmsSpuInfoById(id));
    }

    @Log(title = "新增SPU", businessType = BusinessType.INSERT)
    @Operation(summary = "新增 SPU")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:product:add')")
    public Result<?> add(@Valid @RequestBody ProductSaveBo bo) {
        pmsSpuInfoService.addPmsSpuInfo(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改SPU", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改 SPU")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> update(@Valid @RequestBody ProductSaveBo bo) {
        pmsSpuInfoService.updatePmsSpuInfo(bo);
        return Result.success("修改成功");
    }

    @Log(title = "SPU上架状态", businessType = BusinessType.UPDATE)
    @Operation(summary = "SPU 上架/下架", description = "publishStatus：0-下架 1-上架")
    @PutMapping("/publish-status")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<?> updatePublishStatus(@Valid @RequestBody PublishStatusBo bo) {
        pmsSpuInfoService.updatePublishStatus(bo.getId(), bo.getPublishStatus());
        return Result.success(bo.getPublishStatus() == 1 ? "上架成功" : "下架成功");
    }

    @Log(title = "删除SPU", businessType = BusinessType.DELETE)
    @Operation(summary = "删除 SPU", description = "请求体 ids 为 SPU 主键列表")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:product:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        pmsSpuInfoService.removePmsSpuInfoByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
