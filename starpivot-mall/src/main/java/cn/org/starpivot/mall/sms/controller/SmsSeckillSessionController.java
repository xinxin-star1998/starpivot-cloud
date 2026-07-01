package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillSessionVo;
import cn.org.starpivot.mall.sms.service.SmsSeckillSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-秒杀场次控制器。
 * <p>
 * 提供商城-秒杀场次相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/seckill-session}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-秒杀场次」</li>
 * </ul>
 *
 * @see SmsSeckillSessionService
 */

@RestController
@RequestMapping("/mall/seckill-session")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-秒杀场次")
public class SmsSeckillSessionController {

    private final SmsSeckillSessionService smsSeckillSessionService;

    /**
     * 秒杀场次分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "秒杀场次分页列表")
    @PostMapping("/seckillSessionPageList")
    @PreAuthorize("hasAuthority('mall:seckill:session')")
    public Result<PageResponse<SeckillSessionVo>> pageList(@RequestBody SeckillSessionReqBo reqBo) {
        return Result.success(smsSeckillSessionService.pageList(reqBo));
    }

    /**
     * 启用中的秒杀场次（下拉）。
     * @return 列表数据
     */
    @Operation(summary = "启用中的秒杀场次（下拉）")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('mall:seckill:session')")
    public Result<List<SeckillSessionVo>> listAll() {
        return Result.success(smsSeckillSessionService.listAll());
    }

    /**
     * 秒杀场次详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "秒杀场次详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:seckill:session')")
    public Result<SeckillSessionVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSeckillSessionService.getById(id));
    }

    /**
     * 新增秒杀场次。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增秒杀场次", businessType = BusinessType.INSERT)
    @Operation(summary = "新增秒杀场次")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:seckill:session:add')")
    public Result<?> add(@Valid @RequestBody SeckillSessionSaveBo bo) {
        smsSeckillSessionService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改秒杀场次。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改秒杀场次", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改秒杀场次")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:seckill:session:edit')")
    public Result<?> update(@Valid @RequestBody SeckillSessionSaveBo bo) {
        smsSeckillSessionService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除秒杀场次。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除秒杀场次", businessType = BusinessType.DELETE)
    @Operation(summary = "删除秒杀场次")
    @DeleteMapping("/removeSeckillSession")
    @PreAuthorize("hasAuthority('mall:seckill:session:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsSeckillSessionService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
