package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuLadderVo;
import cn.org.starpivot.mall.sms.service.SmsSkuLadderService;
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
@RequestMapping("/mall/sku-ladder")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-SKU阶梯价")
public class SmsSkuLadderController {

    private final SmsSkuLadderService smsSkuLadderService;

    @Operation(summary = "SKU阶梯价分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:reduction:list')")
    public Result<PageResponse<SkuLadderVo>> pageList(@RequestBody SkuLadderReqBo reqBo) {
        return Result.success(smsSkuLadderService.pageList(reqBo));
    }

    @Operation(summary = "SKU阶梯价详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:reduction:query')")
    public Result<SkuLadderVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsSkuLadderService.getById(id));
    }

    @Log(title = "新增SKU阶梯价", businessType = BusinessType.INSERT)
    @Operation(summary = "新增SKU阶梯价")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:reduction:add')")
    public Result<?> add(@Valid @RequestBody SkuLadderSaveBo bo) {
        smsSkuLadderService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改SKU阶梯价", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改SKU阶梯价")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:reduction:edit')")
    public Result<?> update(@Valid @RequestBody SkuLadderSaveBo bo) {
        smsSkuLadderService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除SKU阶梯价", businessType = BusinessType.DELETE)
    @Operation(summary = "删除SKU阶梯价")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:reduction:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsSkuLadderService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
