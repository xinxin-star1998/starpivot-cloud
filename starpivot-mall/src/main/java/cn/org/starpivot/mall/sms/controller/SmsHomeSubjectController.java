package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeSubjectVo;
import cn.org.starpivot.mall.sms.service.SmsHomeSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-专题活动控制器。
 * <p>
 * 提供商城-专题活动相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/subject}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-专题活动」</li>
 * </ul>
 *
 * @see SmsHomeSubjectService
 */

@RestController
@RequestMapping("/mall/subject")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-专题活动")
public class SmsHomeSubjectController {

    private final SmsHomeSubjectService smsHomeSubjectService;

    /**
     * 专题分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "专题分页列表")
    @PostMapping("/subjectPageList")
    @PreAuthorize("hasAuthority('mall:subject:list')")
    public Result<PageResponse<HomeSubjectVo>> pageList(@RequestBody HomeSubjectReqBo reqBo) {
        return Result.success(smsHomeSubjectService.pageList(reqBo));
    }

    /**
     * 专题详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "专题详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:subject:query')")
    public Result<HomeSubjectVo> getById(@PathVariable("id") Long id) {
        return Result.success(smsHomeSubjectService.getById(id));
    }

    /**
     * 新增专题。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增专题", businessType = BusinessType.INSERT)
    @Operation(summary = "新增专题")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:subject:add')")
    public Result<?> add(@Valid @RequestBody HomeSubjectSaveBo bo) {
        smsHomeSubjectService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改专题。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改专题", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改专题")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:subject:edit')")
    public Result<?> update(@Valid @RequestBody HomeSubjectSaveBo bo) {
        smsHomeSubjectService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除专题。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除专题", businessType = BusinessType.DELETE)
    @Operation(summary = "删除专题")
    @DeleteMapping("/removeSubject")
    @PreAuthorize("hasAuthority('mall:subject:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        smsHomeSubjectService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
