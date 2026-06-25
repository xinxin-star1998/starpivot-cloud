package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.annotation.NoResponseWrapper;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelToolkit;
import cn.org.starpivot.mall.pms.domain.dto.GroupAttrRelationSaveDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrGroupExcel;
import cn.org.starpivot.mall.pms.domain.vo.GroupAttrRelationVO;
import cn.org.starpivot.mall.pms.domain.vo.PmsAttrGroupVO;
import cn.org.starpivot.mall.pms.excel.PmsAttrGroupExcelHandler;
import cn.org.starpivot.mall.pms.service.PmsAttrGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商城-属性分组控制器。
 * <p>
 * 商品属性分组的增删改查、导入导出及属性关联等接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/group}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-属性分组」</li>
 * </ul>
 *
 * @see PmsAttrGroupService
 * @see PmsAttrGroupExcelHandler
 */

@RestController
@RequestMapping("/mall/group")
@RequiredArgsConstructor
@Tag(name = "商城-属性分组", description = "商品属性分组的增删改查、导入导出及属性关联等接口")
public class PmsAttrGroupController
{
    private final PmsAttrGroupService pmsAttrGroupService;
    private final PmsAttrGroupExcelHandler pmsAttrGroupExcelHandler;

    /**
     * 分页查询属性分组列表。
     *
     * @param queryDTO 分页及筛选条件
     * @return 分页查询结果
     */
    @PreAuthorize(
            "hasAnyAuthority('mall:group:query', 'mall:product:query', 'mall:product:add', 'mall:product:edit')")
    @PostMapping("/list")
    public Result<PageResponse<PmsAttrGroupVO>> list(@RequestBody PmsAttrGroupQueryDTO queryDTO)
    {
        PageResponse<PmsAttrGroupVO> page = pmsAttrGroupService.selectPmsAttrGroupPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取属性分组详细信息
     * 
     * @param attrGroupId 属性分组主键
     * @return 属性分组信息
     */
    @PreAuthorize("hasAuthority('mall:group:query')")
    @GetMapping(value = "/{attrGroupId}")
    public Result<PmsAttrGroupVO> getInfo(@PathVariable("attrGroupId") Long attrGroupId)
    {
        PmsAttrGroupVO pmsAttrGroupVO = pmsAttrGroupService.selectPmsAttrGroupByAttrGroupId(attrGroupId);
        return Result.success(pmsAttrGroupVO);
    }

    /**
     * 新增属性分组
     * 
     * @param pmsAttrGroupDTO 属性分组信息
     * @return 操作结果
     */
    @Log(title = "新增属性分组", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('mall:group:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody PmsAttrGroupDTO pmsAttrGroupDTO)
    {
        boolean success = pmsAttrGroupService.insertPmsAttrGroup(pmsAttrGroupDTO);
        return success ? Result.success("新增属性分组成功") : Result.error("新增属性分组失败");
    }

    /**
     * 修改属性分组
     * 
     * @param pmsAttrGroupDTO 属性分组信息
     * @return 操作结果
     */
    @Log(title = "修改属性分组", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('mall:group:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody PmsAttrGroupDTO pmsAttrGroupDTO)
    {
        boolean success = pmsAttrGroupService.updatePmsAttrGroup(pmsAttrGroupDTO);
        return success ? Result.success("修改属性分组成功") : Result.error("修改属性分组失败");
    }

    /**
     * 查询分组可关联的基本属性列表（含是否已关联）。
     *
     * @param attrGroupId 属性分组主键
     * @return 属性关联列表
     */
    @PreAuthorize("hasAuthority('mall:group:query')")
    @GetMapping("/{attrGroupId}/attrs")
    public Result<List<GroupAttrRelationVO>> listGroupAttrs(@PathVariable Long attrGroupId) {
        return Result.success(pmsAttrGroupService.listGroupAttrRelations(attrGroupId));
    }

    /**
     * 保存分组关联的基本属性（全量覆盖本分组）。
     *
     * @param attrGroupId 属性分组主键
     * @param saveDTO 关联属性保存参数
     * @return 操作结果
     */
    @Log(title = "保存属性分组关联", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('mall:group:edit')")
    @PutMapping("/{attrGroupId}/attrs")
    public Result<?> saveGroupAttrs(
            @PathVariable Long attrGroupId, @Valid @RequestBody GroupAttrRelationSaveDTO saveDTO) {
        boolean success = pmsAttrGroupService.saveGroupAttrRelations(attrGroupId, saveDTO);
        return success ? Result.success("关联属性保存成功") : Result.error("关联属性保存失败");
    }
    /**
     * 删除属性分组（支持批量删除，请求体 ids）
     * 
     * @param deleteRequest 删除请求，包含 ids 数组
     * @return 操作结果
     */
    @Log(title = "删除属性分组", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('mall:group:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest)
    {
        List<Long> idList = deleteRequest.getIds();
        if (idList == null || idList.isEmpty()) {
            return Result.error("删除ID不能为空");
        }
        Long[] attrGroupIds = idList.toArray(new Long[0]);
        boolean success = pmsAttrGroupService.deletePmsAttrGroupByAttrGroupIds(attrGroupIds);
        return success ? Result.success("删除属性分组成功") : Result.error("删除属性分组失败");
    }

    /**
     * 导出属性分组为 Excel。
     *
     * @param queryDTO 分页及筛选条件
     * @return Excel 文件响应实体
     */
    @Log(title = "导出属性分组", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAuthority('mall:group:export')")
    @NoResponseWrapper
    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestBody(required = false) PmsAttrGroupQueryDTO queryDTO) {
        return ExcelToolkit.export(pmsAttrGroupExcelHandler, queryDTO, PmsAttrGroupExcel.class);
    }

    /**
     * 从 Excel 批量导入属性分组。
     *
     * @param file 上传的 Excel 或图片文件
     * @param updateSupport 是否允许更新已存在记录
     * @return 操作结果
     * @throws Exception 文件解析或上传异常
     */
    @Log(title = "导入属性分组", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAuthority('mall:group:import')")
    @PostMapping("/import")
    public Result<?> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport)
            throws Exception {
        return ExcelToolkit.importFile(
                file,
                pmsAttrGroupExcelHandler,
                ExcelImportOptions.of(updateSupport),
                PmsAttrGroupExcel.class);
    }

    /**
     * 下载属性分组导入 Excel 模板。
     *
     * @return 模板文件响应实体
     */
    @PreAuthorize("hasAuthority('mall:group:import')")
    @NoResponseWrapper
    @GetMapping("/importTemplate")
    public ResponseEntity<?> importTemplate() {
        return ExcelToolkit.downloadTemplate(
                pmsAttrGroupExcelHandler, null, PmsAttrGroupExcel.class);
    }
}
