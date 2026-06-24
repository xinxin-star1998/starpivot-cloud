package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import cn.org.starpivot.mall.sms.service.SmsHomeAdvService;
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
@RequestMapping("/mall/home-adv")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-首页广告")
public class SmsHomeAdvController {

    private final SmsHomeAdvService smsHomeAdvService;

    @Operation(summary = "首页广告分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:adv:list')")
    public Result<PageResponse<HomeAdvVo>> pageList(@RequestBody HomeAdvReqBo reqBo) {
        return Result.success(smsHomeAdvService.pageList(reqBo));
    }

    @Operation(summary = "首页广告详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:adv:list')")
    public Result<HomeAdvVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsHomeAdvService.getById(id));
    }

    @Log(title = "新增首页广告", businessType = BusinessType.INSERT)
    @Operation(summary = "新增首页广告")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:adv:add')")
    public Result<?> add(@Valid @RequestBody HomeAdvSaveBo bo) {
        smsHomeAdvService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改首页广告", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改首页广告")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:adv:edit')")
    public Result<?> update(@Valid @RequestBody HomeAdvSaveBo bo) {
        smsHomeAdvService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除首页广告", businessType = BusinessType.DELETE)
    @Operation(summary = "删除首页广告")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:adv:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsHomeAdvService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
