package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuFullReductionVo;
import cn.org.starpivot.mall.sms.service.SmsSkuFullReductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-SKU满减控制器。
 * <p>
 * 提供商城-SKU满减相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/sku-full-reduction}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-SKU满减」</li>
 * </ul>
 *
 * @see SmsSkuFullReductionService
 */

@RestController
@RequestMapping("/mall/sku-full-reduction")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SKU满减")
public class SmsSkuFullReductionController {

    private final SmsSkuFullReductionService smsSkuFullReductionService;

    /**
     * SKU满减分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "SKU满减分页列表")
    @PostMapping("/skuFullReductionPageList")
    @PreAuthorize("hasAuthority('mall:reduction:list')")
    public Result<PageResponse<SkuFullReductionVo>> pageList(@RequestBody SkuFullReductionReqBo reqBo) {
        return Result.success(smsSkuFullReductionService.pageList(reqBo));
    }

    /**
     * SKU满减详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "SKU满减详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:reduction:query')")
    public Result<SkuFullReductionVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSkuFullReductionService.getById(id));
    }

    /**
     * 新增SKU满减。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增SKU满减", businessType = BusinessType.INSERT)
    @Operation(summary = "新增SKU满减")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:reduction:add')")
    public Result<?> add(@Valid @RequestBody SkuFullReductionSaveBo bo) {
        smsSkuFullReductionService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改SKU满减。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改SKU满减", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改SKU满减")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:reduction:edit')")
    public Result<?> update(@Valid @RequestBody SkuFullReductionSaveBo bo) {
        smsSkuFullReductionService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除SKU满减。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除SKU满减", businessType = BusinessType.DELETE)
    @Operation(summary = "删除SKU满减")
    @DeleteMapping("/removeSkuFullReduction")
    @PreAuthorize("hasAuthority('mall:reduction:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsSkuFullReductionService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
