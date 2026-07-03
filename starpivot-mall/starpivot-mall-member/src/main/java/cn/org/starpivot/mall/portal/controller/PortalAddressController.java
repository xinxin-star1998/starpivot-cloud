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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端-收货地址控制器。
 * <p>
 * 提供C端-收货地址相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/address}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-收货地址」</li>
 * </ul>
 *
 * @see PortalAddressService
 */

@RestController
@RequestMapping("/portal/address")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-收货地址")
public class PortalAddressController {

    private final PortalAddressService portalAddressService;

    /**
     * 地址列表。
     * @return 列表数据
     */
    @Operation(summary = "地址列表")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalAddressVo>> list() {
        return Result.success(portalAddressService.listByMember(PortalMemberContext.requireMemberId()));
    }

    /**
     * 地址详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "地址详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalAddressVo> getById(@PathVariable("id") Long id) {
        return Result.success(portalAddressService.getById(PortalMemberContext.requireMemberId(), id));
    }

    /**
     * 新增/修改地址。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Operation(summary = "新增/修改地址")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> save(@Valid @RequestBody PortalAddressSaveBo bo) {
        portalAddressService.save(PortalMemberContext.requireMemberId(), bo);
        return Result.success(bo.getId() == null ? "新增成功" : "修改成功");
    }

    /**
     * 修改地址。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Operation(summary = "修改地址")
    @PutMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> update(@Valid @RequestBody PortalAddressSaveBo bo) {
        portalAddressService.save(PortalMemberContext.requireMemberId(), bo);
        return Result.success("修改成功");
    }

    /**
     * 删除地址。
     *
     * @param id 主键 ID
     * @return 操作结果
     */
    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> remove(@PathVariable("id") Long id) {
        portalAddressService.remove(PortalMemberContext.requireMemberId(), id);
        return Result.success("删除成功");
    }
}
