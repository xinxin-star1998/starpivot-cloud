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
import org.springframework.web.bind.annotation.*;

/**
 * 商城-库存工作单控制器。
 * <p>
 * 出库工作单查询与锁库存/扣库存。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/ware-task}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-库存工作单」</li>
 * </ul>
 *
 * @see WmsWareOrderTaskService
 */

@RestController
@RequestMapping("/mall/ware-task")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-库存工作单", description = "出库工作单查询与锁库存/扣库存")
public class WmsWareOrderTaskController {

    private final WmsWareOrderTaskService wmsWareOrderTaskService;

    /**
     * 库存工作单分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "库存工作单分页列表")
    @PostMapping("/wareTaskPageList")
    @PreAuthorize("hasAuthority('mall:task:list')")
    public Result<PageResponse<WareOrderTaskVo>> pageList(@RequestBody WareOrderTaskReqBo reqBo) {
        return Result.success(wmsWareOrderTaskService.pageList(reqBo));
    }

    /**
     * 库存工作单详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "库存工作单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:task:list')")
    public Result<WareOrderTaskVo> getById(@PathVariable("id") Long id) {
        return Result.success(wmsWareOrderTaskService.getDetailById(id));
    }

    /**
     * 根据订单生成库存工作单。
     *
     * @param orderId 主键 ID
     * @return 业务数据
     */
    @Log(title = "生成库存工作单", businessType = BusinessType.INSERT)
    @Operation(summary = "根据订单生成库存工作单")
    @PostMapping("/from-order/{orderId}")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<Long> createFromOrder(@PathVariable("orderId") Long orderId) {
        return Result.success(wmsWareOrderTaskService.createFromOrder(orderId));
    }

    /**
     * 锁定工作单库存。
     *
     * @param id 主键 ID
     * @return 操作结果
     */
    @Log(title = "锁定库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "锁定工作单库存")
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> lockStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.lockStock(id);
        return Result.success("锁定成功");
    }

    /**
     * 扣减工作单库存（出库）。
     *
     * @param id 主键 ID
     * @return 操作结果
     */
    @Log(title = "扣减库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "扣减工作单库存（出库）")
    @PutMapping("/{id}/deduct")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> deductStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.deductStock(id);
        return Result.success("出库成功");
    }

    /**
     * 解锁工作单库存。
     *
     * @param id 主键 ID
     * @return 操作结果
     */
    @Log(title = "解锁库存", businessType = BusinessType.UPDATE)
    @Operation(summary = "解锁工作单库存")
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('mall:task:edit')")
    public Result<?> unlockStock(@PathVariable("id") Long id) {
        wmsWareOrderTaskService.unlockStock(id);
        return Result.success("解锁成功");
    }
}
