package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.SysConfigVO;
import cn.org.starpivot.system.domain.dto.SysConfigDTO;
import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统参数配置控制器。
 * <p>
 * 管理系统运行时可调参数（如注册开关）的增删改查。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /config}</li>
 *   <li>{@link Tag} — OpenAPI 分组「参数配置」</li>
 * </ul>
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Tag(name = "参数配置")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 分页查询参数配置列表。
     *
     * @param queryDTO 查询条件
     * @return 参数配置分页结果
     */
    @PreAuthorize("hasAuthority('system:config:query')")
    @PostMapping("/configPageList")
    public Result<PageResponse<SysConfigVO>> list(@RequestBody SysConfigQueryDTO queryDTO) {
        return Result.success(sysConfigService.selectSysConfigPage(queryDTO));
    }

    /**
     * 根据配置 ID 获取详情。
     *
     * @param configId 配置主键
     * @return 参数配置视图对象
     */
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/{configId}")
    public Result<SysConfigVO> getInfo(@PathVariable Long configId) {
        return Result.success(sysConfigService.selectSysConfigByConfigId(configId));
    }

    /**
     * 新增参数配置。
     *
     * @param sysConfigDTO 配置创建参数
     * @return 操作结果
     */
    @Log(title = "新增参数", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody SysConfigDTO sysConfigDTO) {
        boolean success = sysConfigService.insertSysConfig(sysConfigDTO);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改参数配置。
     *
     * @param sysConfigDTO 配置更新参数
     * @return 操作结果
     */
    @Log(title = "修改参数", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysConfigDTO sysConfigDTO) {
        boolean success = sysConfigService.updateSysConfig(sysConfigDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 批量删除参数配置。
     *
     * @param deleteRequest 待删除配置 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除参数", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:config:delete')")
    @DeleteMapping("/removeConfig")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest.getIds() == null || deleteRequest.getIds().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        boolean success = sysConfigService.deleteSysConfigByConfigIds(
                deleteRequest.getIds().toArray(Long[]::new));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
