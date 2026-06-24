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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/cart")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-购物车", description = "Redis 持久化购物车")
public class PortalCartController {

    private final PortalCartService portalCartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalCartVo> list() {
        return Result.success(portalCartService.listCart(PortalMemberContext.requireMemberId()));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> add(@Valid @RequestBody PortalCartAddBo bo) {
        portalCartService.addItem(PortalMemberContext.requireMemberId(), bo);
        return Result.success("已加入购物车");
    }

    @Operation(summary = "更新购物车条目")
    @PutMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> update(@Valid @RequestBody PortalCartUpdateBo bo) {
        portalCartService.updateItem(PortalMemberContext.requireMemberId(), bo);
        return Result.success("更新成功");
    }

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
