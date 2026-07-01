package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeCategoryHotVo;
import cn.org.starpivot.mall.sms.service.SmsHomeCategoryHotService;
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
@RequestMapping("/mall/category-hot")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-首页分类热门")
public class SmsHomeCategoryHotController {

    private final SmsHomeCategoryHotService smsHomeCategoryHotService;

    @Operation(summary = "分类热门分页列表")
    @PostMapping("/categoryHotPageList")
    @PreAuthorize("hasAuthority('mall:categoryHot:list')")
    public Result<PageResponse<HomeCategoryHotVo>> pageList(@RequestBody HomeCategoryHotReqBo reqBo) {
        return Result.success(smsHomeCategoryHotService.pageList(reqBo));
    }

    @Operation(summary = "分类热门详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:categoryHot:list')")
    public Result<HomeCategoryHotVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsHomeCategoryHotService.getById(id));
    }

    @Log(title = "新增分类热门", businessType = BusinessType.INSERT)
    @Operation(summary = "新增分类热门")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:categoryHot:add')")
    public Result<?> add(@Valid @RequestBody HomeCategoryHotSaveBo bo) {
        smsHomeCategoryHotService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改分类热门", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改分类热门")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:categoryHot:edit')")
    public Result<?> update(@Valid @RequestBody HomeCategoryHotSaveBo bo) {
        smsHomeCategoryHotService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除分类热门", businessType = BusinessType.DELETE)
    @Operation(summary = "删除分类热门")
    @DeleteMapping("/removeCategoryHot")
    @PreAuthorize("hasAuthority('mall:categoryHot:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsHomeCategoryHotService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
