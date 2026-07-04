package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.ums.domain.bo.MemberLevelSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLevelVo;
import cn.org.starpivot.mall.ums.service.UmsMemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-会员等级控制器。
 * <p>
 * 提供商城-会员等级相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/member-level}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-会员等级」</li>
 * </ul>
 *
 * @see UmsMemberLevelService
 */

@RestController
@RequestMapping("/mall/member-level")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员等级")
public class UmsMemberLevelController {

    private final UmsMemberLevelService umsMemberLevelService;

    /**
     * 会员等级列表。
     * @return 列表数据
     */
    @Operation(summary = "会员等级列表")
    @GetMapping("/memberLevelPageList")
    @PreAuthorize("hasAuthority('mall:member:level')")
    public Result<List<MemberLevelVo>> listAll() {
        return Result.success(umsMemberLevelService.listAll());
    }

    /**
     * 会员等级列表（与商城其他模块一致使用 POST /list）。
     */
    @Operation(summary = "会员等级列表")
    @PostMapping("/memberLevelPageList")
    @PreAuthorize("hasAuthority('mall:member:level')")
    public Result<List<MemberLevelVo>> list() {
        return listAll();
    }

    /**
     * 会员等级详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "会员等级详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:member:level')")
    public Result<MemberLevelVo> getById(@PathVariable("id") Long id) {
        return Result.success(umsMemberLevelService.getById(id));
    }

    /**
     * 新增会员等级。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增会员等级", businessType = BusinessType.INSERT)
    @Operation(summary = "新增会员等级")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:member:level:add')")
    public Result<?> add(@Valid @RequestBody MemberLevelSaveBo bo) {
        umsMemberLevelService.add(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改会员等级。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改会员等级", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改会员等级")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:member:level:edit')")
    public Result<?> update(@Valid @RequestBody MemberLevelSaveBo bo) {
        umsMemberLevelService.update(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除会员等级。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除会员等级", businessType = BusinessType.DELETE)
    @Operation(summary = "删除会员等级")
    @DeleteMapping("/removeMemberLevel")
    @PreAuthorize("hasAuthority('mall:member:level:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        umsMemberLevelService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
