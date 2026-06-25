package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartUpdateBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端-购物车控制器。
 * <p>
 * Redis 持久化购物车。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/cart}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-购物车」</li>
 * </ul>
 *
 * @see PortalCartService
 */

@RestController
@RequestMapping("/portal/cart")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-购物车", description = "Redis 持久化购物车")
public class PortalCartController {

    private final PortalCartService portalCartService;

    /**
     * 购物车列表。
     * @return 业务数据
     */
    @Operation(summary = "购物车列表")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalCartVo> list() {
        return Result.success(portalCartService.listCart(PortalMemberContext.requireMemberId()));
    }

    /**
     * 加入购物车。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Operation(summary = "加入购物车")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> add(@Valid @RequestBody PortalCartAddBo bo) {
        portalCartService.addItem(PortalMemberContext.requireMemberId(), bo);
        return Result.success("已加入购物车");
    }

    /**
     * 更新购物车条目。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Operation(summary = "更新购物车条目")
    @PutMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> update(@Valid @RequestBody PortalCartUpdateBo bo) {
        portalCartService.updateItem(PortalMemberContext.requireMemberId(), bo);
        return Result.success("更新成功");
    }

    /**
     * 删除购物车条目。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "删除购物车条目")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        portalCartService.removeItems(PortalMemberContext.requireMemberId(), ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID不能为空");
        }
        return ids;
    }
}
