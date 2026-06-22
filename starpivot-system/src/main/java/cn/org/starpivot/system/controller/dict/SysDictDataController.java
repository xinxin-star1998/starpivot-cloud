package cn.org.starpivot.system.controller.dict;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.DeleteRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.SysDictDataVO;
import cn.org.starpivot.system.domain.dto.SysDictDataDTO;
import cn.org.starpivot.system.domain.dto.SysDictDataQueryDTO;
import cn.org.starpivot.system.service.SysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理控制器。
 * <p>
 * 提供字典数据项的增删改查及按类型批量查询 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/dict/data}</li>
 *   <li>{@link Tag} — OpenAPI 分组「字典数据管理」</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/sys/dict/data")
@RequiredArgsConstructor
@Tag(name = "字典数据管理", description = "字典数据的增删改查等接口")
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;

    /**
     * 分页查询字典数据列表
     */
    @Operation(summary = "分页查询字典数据", description = "根据条件分页查询字典数据列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @PreAuthorize("hasAuthority('system:data:query')")
    @PostMapping("/list")
    public Result<PageResponse<SysDictDataVO>> list(@RequestBody SysDictDataQueryDTO queryDTO) {
        PageResponse<SysDictDataVO> page = sysDictDataService.selectDictDataPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据字典类型查询字典数据
     */
    @Operation(summary = "根据字典类型查询数据", description = "根据字典类型获取该类型下的所有字典数据")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @PreAuthorize("hasAuthority('system:data:query')")
    @GetMapping("/type/{dictType}")
    public Result<List<SysDictDataVO>> getDataByType(@Parameter(description = "字典类型") @PathVariable String dictType) {
        List<SysDictDataVO> list = sysDictDataService.selectDictDataByType(dictType);
        return Result.success(list);
    }

    /**
     * 根据字典编码查询字典数据详情
     */
    @Operation(summary = "获取字典数据详情", description = "根据字典编码获取字典数据的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = SysDictDataVO.class))),
            @ApiResponse(responseCode = "404", description = "字典数据不存在")
    })
    @PreAuthorize("hasAuthority('system:data:query')")
    @GetMapping("/{dictCode}")
    public Result<SysDictDataVO> getInfo(@Parameter(description = "字典编码") @PathVariable Long dictCode) {
        SysDictDataVO dictDataVO = sysDictDataService.selectDictDataById(dictCode);
        return Result.success(dictDataVO);
    }

    /**
     * 新增字典数据
     */
    @Operation(summary = "新增字典数据", description = "创建新字典数据")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @Log(title = "新增字典数据", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:data:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody SysDictDataDTO dictDataDTO) {
        boolean success = sysDictDataService.insertDictData(dictDataDTO);
        return success ? Result.success("新增字典数据成功") : Result.error("新增字典数据失败");
    }

    /**
     * 修改字典数据
     */
    @Operation(summary = "修改字典数据", description = "更新字典数据信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "404", description = "字典数据不存在")
    })
    @Log(title = "修改字典数据", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:data:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysDictDataDTO dictDataDTO) {
        boolean success = sysDictDataService.updateDictData(dictDataDTO);
        return success ? Result.success("修改字典数据成功") : Result.error("修改字典数据失败");
    }

    /**
     * 删除字典数据（支持单删和批量删除）
     */
    @Operation(summary = "删除字典数据", description = "删除字典数据（支持批量删除）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "删除ID为空")
    })
    @Log(title = "删除字典数据", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:data:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> dictCodes = validateIds(deleteRequest.getIds());
        boolean success = sysDictDataService.deleteDictDataByIds(dictCodes);
        return success ? Result.success("删除字典数据成功") : Result.error("删除字典数据失败");
    }

    /**
     * 验证ID列表非空
     */
    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(
                ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}