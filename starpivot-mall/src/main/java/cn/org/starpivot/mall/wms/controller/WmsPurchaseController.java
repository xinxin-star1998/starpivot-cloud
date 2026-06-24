package cn.org.starpivot.mall.wms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailReqBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailSaveBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDoneBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseMergeBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseReqBo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseDetailVo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseVo;
import cn.org.starpivot.mall.wms.service.WmsPurchaseService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/purchase")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-采购", description = "采购单与采购需求")
public class WmsPurchaseController {

    private final WmsPurchaseService wmsPurchaseService;

    @Operation(summary = "采购单分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:purchase:list')")
    public Result<PageResponse<PurchaseVo>> pageList(@RequestBody PurchaseReqBo reqBo) {
        return Result.success(wmsPurchaseService.pageList(reqBo));
    }

    @Operation(summary = "未领取采购单列表")
    @PostMapping("/unreceive/list")
    @PreAuthorize("hasAuthority('mall:purchase:list')")
    public Result<PageResponse<PurchaseVo>> unreceiveList(@RequestBody PurchaseReqBo reqBo) {
        return Result.success(wmsPurchaseService.unreceivePageList(reqBo));
    }

    @Operation(summary = "采购单详情（含明细）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:purchase:query')")
    public Result<PurchaseVo> getById(@PathVariable("id") Long id) {
        return Result.success(wmsPurchaseService.getDetailById(id));
    }

    @Log(title = "合并采购需求", businessType = BusinessType.UPDATE)
    @Operation(summary = "合并采购需求到采购单")
    @PostMapping("/merge")
    @PreAuthorize("hasAuthority('mall:purchase:edit')")
    public Result<?> merge(@Valid @RequestBody PurchaseMergeBo bo) {
        wmsPurchaseService.merge(bo);
        return Result.success("合并成功");
    }

    @Log(title = "领取采购单", businessType = BusinessType.UPDATE)
    @Operation(summary = "领取采购单")
    @PostMapping("/received")
    @PreAuthorize("hasAuthority('mall:purchase:edit')")
    public Result<?> received(@RequestBody List<Long> ids) {
        wmsPurchaseService.received(ids);
        return Result.success("领取成功");
    }

    @Log(title = "完成采购", businessType = BusinessType.UPDATE)
    @Operation(summary = "完成采购并入库")
    @PostMapping("/done")
    @PreAuthorize("hasAuthority('mall:purchase:edit')")
    public Result<?> done(@Valid @RequestBody PurchaseDoneBo bo) {
        wmsPurchaseService.done(bo);
        return Result.success("采购完成");
    }

    @Operation(summary = "采购需求分页列表")
    @PostMapping("/detail/list")
    @PreAuthorize("hasAuthority('mall:purchase:item')")
    public Result<PageResponse<PurchaseDetailVo>> detailPageList(@RequestBody PurchaseDetailReqBo reqBo) {
        return Result.success(wmsPurchaseService.detailPageList(reqBo));
    }

    @Log(title = "新增采购需求", businessType = BusinessType.INSERT)
    @Operation(summary = "新增采购需求")
    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('mall:purchase:item')")
    public Result<?> addDetail(@Valid @RequestBody PurchaseDetailSaveBo bo) {
        wmsPurchaseService.addDetail(bo);
        return Result.success("新增成功");
    }

    @Log(title = "删除采购需求", businessType = BusinessType.DELETE)
    @Operation(summary = "删除采购需求")
    @DeleteMapping("/detail/remove")
    @PreAuthorize("hasAuthority('mall:purchase:item')")
    public Result<?> removeDetail(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = deleteRequest.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        wmsPurchaseService.removeDetails(ids);
        return Result.success("删除成功");
    }
}
