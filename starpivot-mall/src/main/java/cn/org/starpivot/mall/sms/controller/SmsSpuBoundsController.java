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
@RequestMapping("/mall/spu-bounds")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SPU积分")
public class SmsSpuBoundsController {

    private final SmsSpuBoundsService smsSpuBoundsService;

    @Operation(summary = "SPU积分分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:bounds:list')")
    public Result<PageResponse<SpuBoundsVo>> pageList(@RequestBody SpuBoundsReqBo reqBo) {
        return Result.success(smsSpuBoundsService.pageList(reqBo));
    }

    @Operation(summary = "SPU积分详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:bounds:query')")
    public Result<SpuBoundsVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSpuBoundsService.getById(id));
    }

    @Operation(summary = "按 SPU ID 查询积分配置")
    @GetMapping("/spu/{spuId}")
    @PreAuthorize("hasAuthority('mall:bounds:query')")
    public Result<SpuBoundsVo> getBySpuId(@PathVariable("spuId") Long spuId) {
        return Result.success(smsSpuBoundsService.getBySpuId(spuId));
    }

    @Log(title = "新增SPU积分", businessType = BusinessType.INSERT)
    @Operation(summary = "新增SPU积分")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:bounds:add')")
    public Result<?> add(@Valid @RequestBody SpuBoundsSaveBo bo) {
        smsSpuBoundsService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改SPU积分", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改SPU积分")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:bounds:edit')")
    public Result<?> update(@Valid @RequestBody SpuBoundsSaveBo bo) {
        smsSpuBoundsService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除SPU积分", businessType = BusinessType.DELETE)
    @Operation(summary = "删除SPU积分")
    @DeleteMapping("/remove")
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
