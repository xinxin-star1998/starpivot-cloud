package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillPromotionVo;
import cn.org.starpivot.mall.sms.service.SmsSeckillPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-秒杀活动控制器。
 * <p>
 * 提供商城-秒杀活动相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/seckill-promotion}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-秒杀活动」</li>
 * </ul>
 *
 * @see SmsSeckillPromotionService
 */

@RestController
@RequestMapping("/mall/seckill-promotion")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-秒杀活动")
public class SmsSeckillPromotionController {

    private final SmsSeckillPromotionService smsSeckillPromotionService;

    /**
     * 秒杀活动分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "秒杀活动分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:seckill:list')")
    public Result<PageResponse<SeckillPromotionVo>> pageList(@RequestBody SeckillPromotionReqBo reqBo) {
        return Result.success(smsSeckillPromotionService.pageList(reqBo));
    }

    /**
     * 秒杀活动详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "秒杀活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:seckill:query')")
    public Result<SeckillPromotionVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSeckillPromotionService.getById(id));
    }

    /**
     * 新增秒杀活动。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增秒杀活动", businessType = BusinessType.INSERT)
    @Operation(summary = "新增秒杀活动")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:seckill:add')")
    public Result<?> add(@Valid @RequestBody SeckillPromotionSaveBo bo) {
        smsSeckillPromotionService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改秒杀活动。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改秒杀活动", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改秒杀活动")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:seckill:edit')")
    public Result<?> update(@Valid @RequestBody SeckillPromotionSaveBo bo) {
        smsSeckillPromotionService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除秒杀活动。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除秒杀活动", businessType = BusinessType.DELETE)
    @Operation(summary = "删除秒杀活动")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:seckill:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsSeckillPromotionService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
