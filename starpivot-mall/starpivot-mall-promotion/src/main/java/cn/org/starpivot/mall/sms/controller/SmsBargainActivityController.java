package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.BargainActivityVo;
import cn.org.starpivot.mall.sms.service.SmsBargainActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-砍价活动后台控制器。
 *
 * @see SmsBargainActivityService
 */
@RestController
@RequestMapping("/mall/bargain-activity")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-砍价活动")
public class SmsBargainActivityController {

    private final SmsBargainActivityService smsBargainActivityService;

    @Operation(summary = "砍价活动分页列表")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('mall:bargain:list')")
    public Result<PageResponse<BargainActivityVo>> pageList(@RequestBody BargainActivityReqBo reqBo) {
        return Result.success(smsBargainActivityService.pageList(reqBo));
    }

    @Operation(summary = "砍价活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:bargain:query')")
    public Result<BargainActivityVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsBargainActivityService.getById(id));
    }

    @Log(title = "新增砍价活动", businessType = BusinessType.INSERT)
    @Operation(summary = "新增砍价活动")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:bargain:add')")
    public Result<?> add(@Valid @RequestBody BargainActivitySaveBo bo) {
        smsBargainActivityService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改砍价活动", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改砍价活动")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:bargain:edit')")
    public Result<?> update(@Valid @RequestBody BargainActivitySaveBo bo) {
        smsBargainActivityService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除砍价活动", businessType = BusinessType.DELETE)
    @Operation(summary = "删除砍价活动")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:bargain:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = deleteRequest.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsBargainActivityService.removeByIds(ids);
        return Result.success("删除成功");
    }
}
