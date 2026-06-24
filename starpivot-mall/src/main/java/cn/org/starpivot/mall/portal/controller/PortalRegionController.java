package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/region")
@RequiredArgsConstructor
@Tag(name = "C端-省市区", description = "收货地址省市区懒加载（公开）")
public class PortalRegionController {

    private final AddressService addressService;

    @Operation(summary = "懒加载子级地区", description = "parentCode 省略或为 0 时返回省级")
    @GetMapping("/children")
    public Result<List<AddressVO>> children(
            @RequestParam(value = "parentCode", required = false, defaultValue = "0") String parentCode) {
        return Result.success(addressService.listChildren(parentCode));
    }
}
