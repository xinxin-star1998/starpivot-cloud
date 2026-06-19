package cn.org.starpivot.common.excel;

import java.util.Collections;
import java.util.List;

/**
 * Excel 导入导出业务处理器：各模块实现此接口并委托给 Service 完成落库逻辑。
 *
 * @param <T> Excel 行类型（带 EasyExcel 注解）
 * @param <Q> 导出/模板查询参数类型
 */
public interface ExcelBizHandler<T, Q> {

    /** 按查询条件加载导出行 */
    List<T> listForExport(Q query);

    /** 导入行数据 */
    ExcelImportResult importRows(List<T> rows, ExcelImportOptions options);

    /** 模板示例行（可选，用于生成带示例的导入模板） */
    default List<T> templateSampleRows(Q query) {
        return Collections.emptyList();
    }

    /** 工作表名称 */
    default String sheetName(Q query) {
        return "数据";
    }

    /** 导出文件名（含 .xlsx） */
    default String exportFileName(Q query) {
        return "export_" + System.currentTimeMillis() + ".xlsx";
    }

    /** 导入模板文件名（含 .xlsx） */
    default String templateFileName(Q query) {
        return "import_template.xlsx";
    }
}
