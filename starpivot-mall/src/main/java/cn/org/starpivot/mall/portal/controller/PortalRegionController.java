package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端-省市区控制器。
 * <p>
 * 收货地址省市区懒加载（公开）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/region}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-省市区」</li>
 * </ul>
 *
 * @see AddressService
 */

@RestController
@RequestMapping("/portal/region")
@RequiredArgsConstructor
@Tag(name = "C端-省市区", description = "收货地址省市区懒加载（公开）")
public class PortalRegionController {

    private final AddressService addressService;

    /**
     * 懒加载子级地区。
     *
     * @param parentCode parentCode 参数
     * @return 列表数据
     */
    @Operation(summary = "懒加载子级地区", description = "parentCode 省略或为 0 时返回省级")
    @GetMapping("/children")
    public Result<List<AddressVO>> children(
            @RequestParam(value = "parentCode", required = false, defaultValue = "0") String parentCode) {
        return Result.success(addressService.listChildren(parentCode));
    }
}
