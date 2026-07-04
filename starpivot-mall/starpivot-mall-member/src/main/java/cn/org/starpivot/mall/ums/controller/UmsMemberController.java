package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.ums.domain.bo.MemberReqBo;
import cn.org.starpivot.mall.ums.domain.bo.MemberSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberVo;
import cn.org.starpivot.mall.ums.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商城-会员控制器。
 * <p>
 * 提供商城-会员相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/member}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-会员」</li>
 * </ul>
 *
 * @see UmsMemberService
 */

@RestController
@RequestMapping("/mall/member")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员")
public class UmsMemberController {

    private final UmsMemberService umsMemberService;

    /**
     * 会员分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "会员分页列表")
    @PostMapping("/memberPageList")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberVo>> pageList(@RequestBody MemberReqBo reqBo) {
        return Result.success(umsMemberService.pageList(reqBo));
    }

    /**
     * 会员详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "会员详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<MemberVo> getById(@PathVariable("id") Long id) {
        return Result.success(umsMemberService.getById(id));
    }

    /**
     * 修改会员。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改会员", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改会员")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:member:edit')")
    public Result<?> update(@Valid @RequestBody MemberSaveBo bo) {
        umsMemberService.update(bo);
        return Result.success("修改成功");
    }
}
