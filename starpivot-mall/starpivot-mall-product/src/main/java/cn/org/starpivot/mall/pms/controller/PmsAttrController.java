package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.annotation.NoResponseWrapper;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelToolkit;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrExcel;
import cn.org.starpivot.mall.pms.domain.vo.PmsAttrVO;
import cn.org.starpivot.mall.pms.excel.PmsAttrExcelHandler;
import cn.org.starpivot.mall.pms.service.PmsAttrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商城-商品属性控制器。
 * <p>
 * 基本属性、销售属性的增删改查与导入导出等接口。基本属性与销售属性共用本 Controller，
 * 通过请求体 {@code attrType} 区分：1-基本属性（mall:base:*），0-销售属性（mall:sale:*）。
 * 与属性分组的绑定见 DTO 的 {@code attrGroupId}，由 Service 写入
 * {@code pms_attr_attrgroup_relation}。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/attr}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-商品属性」</li>
 * </ul>
 *
 * @see PmsAttrService
 * @see PmsAttrExcelHandler
 */

@RestController
@RequestMapping("/mall/attr")
@RequiredArgsConstructor
@Tag(name = "商城-商品属性", description = "基本属性、销售属性的增删改查与导入导出等接口")
public class PmsAttrController {

    private final PmsAttrService pmsAttrService;
    private final PmsAttrExcelHandler pmsAttrExcelHandler;

    /**
     * 分页查询商品属性列表。
     * <p>queryDTO.attrType 必填；attrGroupId 仅作展示回填，不作为列表筛选条件。</p>
     *
     * @param queryDTO 分页及筛选条件
     * @return 分页查询结果
     */
    @PreAuthorize(
            "hasAnyAuthority('mall:base:query', 'mall:sale:query', 'mall:product:query', 'mall:product:add', 'mall:product:edit')")
    @PostMapping("/attrPageList")
    public Result<PageResponse<PmsAttrVO>> list(@RequestBody PmsAttrQueryDTO queryDTO) {
        PageResponse<PmsAttrVO> page = pmsAttrService.selectPmsAttrPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据属性 ID 获取详情（含关联表中的 attrGroupId、attrSort）。
     *
     * @param attrId 属性主键
     * @return 属性视图对象
     */
    @PreAuthorize("hasAnyAuthority('mall:base:query', 'mall:sale:query')")
    @GetMapping("/{attrId}")
    public Result<PmsAttrVO> getInfo(@PathVariable("attrId") Long attrId) {
        PmsAttrVO pmsAttrVO = pmsAttrService.selectPmsAttrByAttrId(attrId);
        return Result.success(pmsAttrVO);
    }

    /**
     * 新增商品属性；可选传 attrGroupId、attrSort 建立分组关联。
     *
     * @param pmsAttrDTO 商品属性信息
     * @return 操作结果
     */
    @Log(title = "新增商品属性", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('mall:base:add', 'mall:sale:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody PmsAttrDTO pmsAttrDTO) {
        boolean success = pmsAttrService.insertPmsAttr(pmsAttrDTO);
        return success ? Result.success("新增商品属性成功") : Result.error("新增商品属性失败");
    }

    /**
     * 修改商品属性；传 attrGroupId=null 可解除与分组的关联。
     *
     * @param pmsAttrDTO 商品属性信息
     * @return 操作结果
     */
    @Log(title = "修改商品属性", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('mall:base:edit', 'mall:sale:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody PmsAttrDTO pmsAttrDTO) {
        boolean success = pmsAttrService.updatePmsAttr(pmsAttrDTO);
        return success ? Result.success("修改商品属性成功") : Result.error("修改商品属性失败");
    }

    /**
     * 批量删除商品属性（同时删除 pms_attr_attrgroup_relation 中对应行）。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除商品属性", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('mall:base:delete', 'mall:sale:delete')")
    @DeleteMapping("/removeAttr")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> idList = deleteRequest.getIds();
        if (idList == null || idList.isEmpty()) {
            return Result.error("删除ID不能为空");
        }
        Long[] attrIds = idList.toArray(new Long[0]);
        boolean success = pmsAttrService.deletePmsAttrByAttrIds(attrIds);
        return success ? Result.success("删除商品属性成功") : Result.error("删除商品属性失败");
    }

    /**
     * 导出商品属性为 Excel（请求体须含 attrType）。
     *
     * @param queryDTO 分页及筛选条件
     * @return Excel 文件响应实体
     */
    @Log(title = "导出商品属性", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAnyAuthority('mall:base:export', 'mall:sale:export')")
    @NoResponseWrapper
    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestBody PmsAttrQueryDTO queryDTO) {
        if (queryDTO == null || queryDTO.getAttrType() == null) {
            return ResponseEntity.badRequest().body(Result.error("导出参数 attrType 不能为空"));
        }
        return ExcelToolkit.export(pmsAttrExcelHandler, queryDTO, PmsAttrExcel.class);
    }

    /**
     * 从 Excel 批量导入商品属性。
     *
     * @param file 上传的 Excel 或图片文件
     * @param attrType 属性类型（1-基本属性，0-销售属性）
     * @param updateSupport 是否允许更新已存在记录
     * @return 操作结果
     * @throws Exception 文件解析或上传异常
     */
    @Log(title = "导入商品属性", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAnyAuthority('mall:base:import', 'mall:sale:import')")
    @PostMapping("/import")
    public Result<?> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("attrType") Integer attrType,
            @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport)
            throws Exception {
        if (attrType == null) {
            return Result.error("attrType 不能为空");
        }
        ExcelImportOptions options =
                ExcelImportOptions.builder().updateSupport(updateSupport).param("attrType", attrType).build();
        return ExcelToolkit.importFile(file, pmsAttrExcelHandler, options, PmsAttrExcel.class);
    }

    /**
     * 下载商品属性导入 Excel 模板。
     *
     * @param attrType 属性类型（1-基本属性，0-销售属性）
     * @return 模板文件响应实体
     */
    @PreAuthorize("hasAnyAuthority('mall:base:import', 'mall:sale:import')")
    @NoResponseWrapper
    @GetMapping("/importTemplate")
    public ResponseEntity<?> importTemplate(@RequestParam("attrType") Integer attrType) {
        if (attrType == null) {
            return ResponseEntity.badRequest().body(Result.error("attrType 不能为空"));
        }
        PmsAttrQueryDTO query = new PmsAttrQueryDTO();
        query.setAttrType(attrType);
        return ExcelToolkit.downloadTemplate(pmsAttrExcelHandler, query, PmsAttrExcel.class);
    }
}
