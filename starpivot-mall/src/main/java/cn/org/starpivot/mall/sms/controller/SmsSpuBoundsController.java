package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SpuBoundsVo;
import cn.org.starpivot.mall.sms.service.SmsSpuBoundsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-SPU积分控制器。
 * <p>
 * 提供商城-SPU积分相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/spu-bounds}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-SPU积分」</li>
 * </ul>
 *
 * @see SmsSpuBoundsService
 */

@RestController
@RequestMapping("/mall/spu-bounds")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SPU积分")
public class SmsSpuBoundsController {

    private final SmsSpuBoundsService smsSpuBoundsService;

    /**
     * SPU积分分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "SPU积分分页列表")
    @PostMapping("/spuBoundsPageList")
    @PreAuthorize("hasAuthority('mall:bounds:list')")
    public Result<PageResponse<SpuBoundsVo>> pageList(@RequestBody SpuBoundsReqBo reqBo) {
        return Result.success(smsSpuBoundsService.pageList(reqBo));
    }

    /**
     * SPU积分详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "SPU积分详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:bounds:query')")
    public Result<SpuBoundsVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSpuBoundsService.getById(id));
    }

    /**
     * 按 SPU ID 查询积分配置。
     *
     * @param spuId 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "按 SPU ID 查询积分配置")
    @GetMapping("/spu/{spuId}")
    @PreAuthorize("hasAuthority('mall:bounds:query')")
    public Result<SpuBoundsVo> getBySpuId(@PathVariable("spuId") Long spuId) {
        return Result.success(smsSpuBoundsService.getBySpuId(spuId));
    }

    /**
     * 新增SPU积分。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增SPU积分", businessType = BusinessType.INSERT)
    @Operation(summary = "新增SPU积分")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:bounds:add')")
    public Result<?> add(@Valid @RequestBody SpuBoundsSaveBo bo) {
        smsSpuBoundsService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改SPU积分。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改SPU积分", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改SPU积分")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:bounds:edit')")
    public Result<?> update(@Valid @RequestBody SpuBoundsSaveBo bo) {
        smsSpuBoundsService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除SPU积分。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除SPU积分", businessType = BusinessType.DELETE)
    @Operation(summary = "删除SPU积分")
    @DeleteMapping("/removeSpuBounds")
    @PreAuthorize("hasAuthority('mall:bounds:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsSpuBoundsService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
