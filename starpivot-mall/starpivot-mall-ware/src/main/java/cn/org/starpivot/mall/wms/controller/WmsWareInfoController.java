package cn.org.starpivot.mall.wms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoReqBo;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoSaveBo;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareInfoVo;
import cn.org.starpivot.mall.wms.service.WmsWareInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-仓库控制器。
 * <p>
 * 仓库 CRUD（表 wms_ware_info）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/wareinfo}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-仓库」</li>
 * </ul>
 *
 * @see WmsWareInfoService
 */

@RestController
@RequestMapping("/mall/wareinfo")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-仓库", description = "仓库 CRUD（表 wms_ware_info）")
public class WmsWareInfoController {

    private final WmsWareInfoService wmsWareInfoService;

    /**
     * 仓库分页列表。
     *
     * @param wmsWareInfoReqBo 业务请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "仓库分页列表")
    @PostMapping("/wareInfoPageList")
    @PreAuthorize("hasAuthority('mall:ware:query')")
    public Result<PageResponse<WmsWareInfoVo>> pageList(@RequestBody WmsWareInfoReqBo wmsWareInfoReqBo) {
        return Result.success(wmsWareInfoService.getWmsWareInfoPageList(wmsWareInfoReqBo));
    }

    /**
     * 仓库详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "仓库详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:ware:query')")
    public Result<WmsWareInfoVo> getById(@PathVariable("id") Long id) {
        return Result.success(wmsWareInfoService.getById(id));
    }

    /**
     * 新增仓库。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "新增仓库", businessType = BusinessType.INSERT)
    @Operation(summary = "新增仓库")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:ware:add')")
    public Result<?> add(@Valid @RequestBody WmsWareInfoSaveBo bo) {
        wmsWareInfoService.addWare(bo);
        return Result.success("新增成功");
    }

    /**
     * 修改仓库。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "修改仓库", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改仓库")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:ware:edit')")
    public Result<?> update(@Valid @RequestBody WmsWareInfoSaveBo bo) {
        wmsWareInfoService.updateWare(bo);
        return Result.success("修改成功");
    }

    /**
     * 删除仓库。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除仓库", businessType = BusinessType.DELETE)
    @Operation(summary = "删除仓库", description = "请求体 ids 为仓库主键列表")
    @DeleteMapping("/removeWareInfo")
    @PreAuthorize("hasAuthority('mall:ware:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        wmsWareInfoService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
