package cn.org.starpivot.mall.pms.excel;

import cn.org.starpivot.common.excel.ExcelBizHandler;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrGroupExcel;
import cn.org.starpivot.mall.pms.service.PmsAttrGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 属性分组 Excel 导入导出处理器
 */
@Component
@RequiredArgsConstructor
public class PmsAttrGroupExcelHandler implements ExcelBizHandler<PmsAttrGroupExcel, PmsAttrGroupQueryDTO> {

    private final PmsAttrGroupService pmsAttrGroupService;

    @Override
    public List<PmsAttrGroupExcel> listForExport(PmsAttrGroupQueryDTO query) {
        return pmsAttrGroupService.listForExport(query);
    }

    @Override
    public ExcelImportResult importRows(List<PmsAttrGroupExcel> rows, ExcelImportOptions options) {
        int count = pmsAttrGroupService.importFromExcel(rows, options.isUpdateSupport());
        return ExcelImportResult.allSuccess(count);
    }

    @Override
    public String sheetName(PmsAttrGroupQueryDTO query) {
        return "属性分组";
    }

    @Override
    public String exportFileName(PmsAttrGroupQueryDTO query) {
        return "pms_attr_group_" + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public String templateFileName(PmsAttrGroupQueryDTO query) {
        return "pms_attr_group_import_template.xlsx";
    }
}
