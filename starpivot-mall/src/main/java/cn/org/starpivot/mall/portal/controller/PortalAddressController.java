package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalAddressSaveBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalAddressVo;
import cn.org.starpivot.mall.portal.service.PortalAddressService;
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
@RequestMapping("/portal/address")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-收货地址")
public class PortalAddressController {

    private final PortalAddressService portalAddressService;

    @Operation(summary = "地址列表")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalAddressVo>> list() {
        return Result.success(portalAddressService.listByMember(PortalMemberContext.requireMemberId()));
    }

    @Operation(summary = "地址详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalAddressVo> getById(@PathVariable("id") Long id) {
        return Result.success(portalAddressService.getById(PortalMemberContext.requireMemberId(), id));
    }

    @Operation(summary = "新增/修改地址")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> save(@Valid @RequestBody PortalAddressSaveBo bo) {
        portalAddressService.save(PortalMemberContext.requireMemberId(), bo);
        return Result.success(bo.getId() == null ? "新增成功" : "修改成功");
    }

    @Operation(summary = "修改地址")
    @PutMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> update(@Valid @RequestBody PortalAddressSaveBo bo) {
        portalAddressService.save(PortalMemberContext.requireMemberId(), bo);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> remove(@PathVariable("id") Long id) {
        portalAddressService.remove(PortalMemberContext.requireMemberId(), id);
        return Result.success("删除成功");
    }
}
