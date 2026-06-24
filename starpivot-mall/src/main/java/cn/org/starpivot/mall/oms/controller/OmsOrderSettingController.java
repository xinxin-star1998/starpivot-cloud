package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.oms.entity.OmsOrderSetting;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单设置
 */
@RestController
@RequestMapping("/mall/order-setting")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-订单设置", description = "订单超时与自动确认规则")
public class OmsOrderSettingController {

    private final OmsOrderSettingService omsOrderSettingService;

    @Operation(summary = "获取订单设置")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:order:setting')")
    public Result<OmsOrderSetting> getSetting() {
        return Result.success(omsOrderSettingService.getSetting());
    }

    @Log(title = "订单设置", businessType = BusinessType.UPDATE)
    @Operation(summary = "更新订单设置")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:order:setting')")
    public Result<?> updateSetting(@Valid @RequestBody OmsOrderSetting setting) {
        omsOrderSettingService.updateSetting(setting);
        return Result.success("保存成功");
    }
}
