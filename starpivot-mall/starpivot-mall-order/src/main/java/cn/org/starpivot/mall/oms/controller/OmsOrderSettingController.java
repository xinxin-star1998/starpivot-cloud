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
 * 商城-订单设置控制器。
 * <p>
 * 订单超时与自动确认规则。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/order-setting}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-订单设置」</li>
 * </ul>
 *
 * @see OmsOrderSettingService
 */

@RestController
@RequestMapping("/mall/order-setting")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-订单设置", description = "订单超时与自动确认规则")
public class OmsOrderSettingController {

    private final OmsOrderSettingService omsOrderSettingService;

    /**
     * 获取订单设置。
     * @return 业务数据
     */
    @Operation(summary = "获取订单设置")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:order:setting')")
    public Result<OmsOrderSetting> getSetting() {
        return Result.success(omsOrderSettingService.getSetting());
    }

    /**
     * 更新订单设置。
     *
     * @param setting setting 参数
     * @return 操作结果
     */
    @Log(title = "订单设置", businessType = BusinessType.UPDATE)
    @Operation(summary = "更新订单设置")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:order:setting')")
    public Result<?> updateSetting(@Valid @RequestBody OmsOrderSetting setting) {
        omsOrderSettingService.updateSetting(setting);
        return Result.success("保存成功");
    }
}
