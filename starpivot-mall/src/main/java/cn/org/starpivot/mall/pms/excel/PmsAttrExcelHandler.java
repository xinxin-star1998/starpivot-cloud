package cn.org.starpivot.mall.pms.excel;

import cn.org.starpivot.common.excel.ExcelBizHandler;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrExcel;
import cn.org.starpivot.mall.pms.service.PmsAttrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品属性 Excel 导入导出处理器
 */
@Component
@RequiredArgsConstructor
public class PmsAttrExcelHandler implements ExcelBizHandler<PmsAttrExcel, PmsAttrQueryDTO> {

    private final PmsAttrService pmsAttrService;

    @Override
    public List<PmsAttrExcel> listForExport(PmsAttrQueryDTO query) {
        return pmsAttrService.listForExport(query);
    }

    @Override
    public ExcelImportResult importRows(List<PmsAttrExcel> rows, ExcelImportOptions options) {
        Integer attrType = options.getIntParam("attrType");
        if (attrType == null) {
            throw new BizException("attrType 不能为空");
        }
        int count = pmsAttrService.importFromExcel(rows, attrType, options.isUpdateSupport());
        return ExcelImportResult.allSuccess(count);
    }

    @Override
    public String sheetName(PmsAttrQueryDTO query) {
        return Integer.valueOf(1).equals(query != null ? query.getAttrType() : null) ? "基本属性" : "销售属性";
    }

    @Override
    public String exportFileName(PmsAttrQueryDTO query) {
        String prefix =
                Integer.valueOf(1).equals(query != null ? query.getAttrType() : null)
                        ? "pms_base_attr_"
                        : "pms_sale_attr_";
        return prefix + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public String templateFileName(PmsAttrQueryDTO query) {
        return Integer.valueOf(1).equals(query != null ? query.getAttrType() : null)
                ? "pms_base_attr_import_template.xlsx"
                : "pms_sale_attr_import_template.xlsx";
    }
}
