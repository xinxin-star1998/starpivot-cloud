package cn.org.starpivot.mall.wms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.domain.dto.AddressDTO;
import cn.org.starpivot.mall.wms.domain.dto.AddressQueryDTO;
import cn.org.starpivot.mall.wms.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-省市区控制器。
 * <p>
 * 省市区地址 CRUD（表 address）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/address}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-省市区」</li>
 * </ul>
 *
 * @see AddressService
 */

@RestController
@RequestMapping("/mall/address")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-省市区", description = "省市区地址 CRUD（表 address）")
public class AddressController {

    private final AddressService addressService;

    @Operation(
            summary = "懒加载子节点",
            description = "仅返回指定父级编码下的直接子级；parentCode 省略或为 0 时返回省级")
    /**
     * children。
     *
     * @param parentCode parentCode 参数
     * @return 列表数据
     */
    @GetMapping("/children")
    @PreAuthorize("hasAuthority('mall:address:query')")
    public Result<List<AddressVO>> children(
            @RequestParam(value = "parentCode", required = false, defaultValue = "0") String parentCode) {
        return Result.success(addressService.listChildren(parentCode));
    }

    /**
     * 搜索地区。
     *
     * @param code code 参数
     * @param parentCode parentCode 参数
     * @param name name 参数
     * @param level level 参数
     * @return 列表数据
     */
    @Operation(summary = "搜索地区", description = "扁平列表，最多 200 条；至少传 name/code/parentCode/level 之一")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('mall:address:query')")
    public Result<List<AddressVO>> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long level) {
        AddressQueryDTO queryDTO = new AddressQueryDTO();
        queryDTO.setCode(code);
        queryDTO.setParentCode(parentCode);
        queryDTO.setName(name);
        queryDTO.setLevel(level);
        return Result.success(addressService.searchAddress(queryDTO));
    }

    /**
     * 省市区详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "省市区详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:address:query')")
    public Result<AddressVO> getInfo(@PathVariable("id") Long id) {
        return Result.success(addressService.selectAddressById(id));
    }

    /**
     * 新增省市区。
     *
     * @param addressDTO 数据传输对象
     * @return 操作结果
     */
    @Log(title = "新增省市区", businessType = BusinessType.INSERT)
    @Operation(summary = "新增省市区")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:address:add')")
    public Result<?> add(@Valid @RequestBody AddressDTO addressDTO) {
        addressService.insertAddress(addressDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改省市区。
     *
     * @param addressDTO 数据传输对象
     * @return 操作结果
     */
    @Log(title = "修改省市区", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改省市区")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:address:edit')")
    public Result<?> edit(@Valid @RequestBody AddressDTO addressDTO) {
        addressService.updateAddress(addressDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除省市区。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除省市区", businessType = BusinessType.DELETE)
    @Operation(summary = "删除省市区", description = "请求体 ids 为地址主键列表")
    @DeleteMapping("/removeAddress")
    @PreAuthorize("hasAuthority('mall:address:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        addressService.deleteAddressByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
