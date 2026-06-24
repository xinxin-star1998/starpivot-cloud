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
@RequestMapping("/mall/seckill-promotion")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-秒杀活动")
public class SmsSeckillPromotionController {

    private final SmsSeckillPromotionService smsSeckillPromotionService;

    @Operation(summary = "秒杀活动分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:seckill:list')")
    public Result<PageResponse<SeckillPromotionVo>> pageList(@RequestBody SeckillPromotionReqBo reqBo) {
        return Result.success(smsSeckillPromotionService.pageList(reqBo));
    }

    @Operation(summary = "秒杀活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:seckill:query')")
    public Result<SeckillPromotionVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSeckillPromotionService.getById(id));
    }

    @Log(title = "新增秒杀活动", businessType = BusinessType.INSERT)
    @Operation(summary = "新增秒杀活动")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:seckill:add')")
    public Result<?> add(@Valid @RequestBody SeckillPromotionSaveBo bo) {
        smsSeckillPromotionService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改秒杀活动", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改秒杀活动")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:seckill:edit')")
    public Result<?> update(@Valid @RequestBody SeckillPromotionSaveBo bo) {
        smsSeckillPromotionService.update(bo);
        return Result.success("修改成功");
    }

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
