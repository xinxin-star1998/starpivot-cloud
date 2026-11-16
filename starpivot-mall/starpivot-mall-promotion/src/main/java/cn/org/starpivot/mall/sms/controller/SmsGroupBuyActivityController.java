package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.GroupBuyActivityVo;
import cn.org.starpivot.mall.sms.service.SmsGroupBuyActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-拼团活动后台控制器。
 *
 * @see SmsGroupBuyActivityService
 */
@RestController
@RequestMapping("/mall/group-buy-activity")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-拼团活动")
public class SmsGroupBuyActivityController {

    private final SmsGroupBuyActivityService smsGroupBuyActivityService;

    @Operation(summary = "拼团活动分页列表")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('mall:groupbuy:list')")
    public Result<PageResponse<GroupBuyActivityVo>> pageList(@RequestBody GroupBuyActivityReqBo reqBo) {
        return Result.success(smsGroupBuyActivityService.pageList(reqBo));
    }

    @Operation(summary = "拼团活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:groupbuy:query')")
    public Result<GroupBuyActivityVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsGroupBuyActivityService.getById(id));
    }

    @Log(title = "新增拼团活动", businessType = BusinessType.INSERT)
    @Operation(summary = "新增拼团活动")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:groupbuy:add')")
    public Result<?> add(@Valid @RequestBody GroupBuyActivitySaveBo bo) {
        smsGroupBuyActivityService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改拼团活动", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改拼团活动")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:groupbuy:edit')")
    public Result<?> update(@Valid @RequestBody GroupBuyActivitySaveBo bo) {
        smsGroupBuyActivityService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除拼团活动", businessType = BusinessType.DELETE)
    @Operation(summary = "删除拼团活动")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:groupbuy:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = deleteRequest.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsGroupBuyActivityService.removeByIds(ids);
        return Result.success("删除成功");
    }
}
