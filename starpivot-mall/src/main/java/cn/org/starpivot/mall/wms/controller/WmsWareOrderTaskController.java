package cn.org.starpivot.mall.wms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.wms.domain.bo.WareOrderTaskReqBo;
import cn.org.starpivot.mall.wms.domain.vo.WareOrderTaskVo;
import cn.org.starpivot.mall.wms.service.WmsWareOrderTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/ware-task")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-库存工作单", description = "出库工作单查询与锁库存/扣库存")
public class WmsWareOrderTaskController {

    private final WmsWareOrderTaskService wmsWareOrderTaskService;

    @Operation(summary = "库存工作单分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:task:list')")
    public Result<PageResponse<WareOrderTaskVo>> pageList(@RequestBody WareOrderTaskReqBo reqBo) {
        return Result.success(wmsWareOrderTaskService.pageList(reqBo));
    }

    @Operation(summary = "库存工作单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:task:list')")
    public Result<WareOrderTaskVo> getById(@PathVariable("id") Long id) {
        return Result.success(wmsWareOrderTaskService.getDetailById(id));
    }

    @Log(title = "生成库存工作单", businessType = BusinessType.INSERT)
    @Operation(summary = "根据订单生成库存工作单")
    @PostMapping("/from-order/{orderId}")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<Long> createFromOrder(@PathVariable("orderId") Long orderId) {
        return Result.success(wmsWareOrderTaskService.createFromOrder(orderId));
    }

    @Log(title = "锁定库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "锁定工作单库存")
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> lockStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.lockStock(id);
        return Result.success("锁定成功");
    }

    @Log(title = "扣减库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "扣减工作单库存（出库）")
    @PutMapping("/{id}/deduct")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> deductStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.deductStock(id);
        return Result.success("出库成功");
    }

    @Log(title = "解锁库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "解锁工作单库存")
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> unlockStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.unlockStock(id);
        return Result.success("解锁成功");
    }
}
