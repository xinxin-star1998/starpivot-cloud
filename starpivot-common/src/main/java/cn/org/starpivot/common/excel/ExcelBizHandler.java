package cn.org.starpivot.common.excel;

import java.util.Collections;
import java.util.List;

/**
 * Excel 导入导出业务处理器：各模块实现此接口并委托 Service 完成落库逻辑。
 * <p>
 * 与 {@link ExcelToolkit} 配合，Controller 仅需一行调用即可完成导出、模板下载与导入。
 * </p>
 *
 * @param <T> Excel 行类型（带 EasyExcel 注解）
 * @param <Q> 导出/模板查询参数类型
 */
public interface ExcelBizHandler<T, Q> {

    /**
     * 按查询条件加载待导出行。
     *
     * @param query 导出筛选条件
     * @return 导出行列表，空列表时 {@link ExcelToolkit} 返回错误响应
     */
    List<T> listForExport(Q query);

    /**
     * 批量导入 Excel 行并落库。
     *
     * @param rows    解析后的行数据
     * @param options 导入选项（是否覆盖更新、扩展参数等）
     * @return 导入结果统计
     */
    ExcelImportResult importRows(List<T> rows, ExcelImportOptions options);

    /**
     * 模板示例行（可选，用于生成带示例的导入模板）。
     *
     * @param query 查询条件
     * @return 示例行，默认空列表
     */
    default List<T> templateSampleRows(Q query) {
        return Collections.emptyList();
    }

    /**
     * Excel 工作表名称。
     *
     * @param query 查询条件
     * @return 工作表名，默认 {@code 数据}
     */
    default String sheetName(Q query) {
        return "数据";
    }

    /**
     * 导出文件名（含 {@code .xlsx} 后缀）。
     *
     * @param query 查询条件
     * @return 下载文件名
     */
    default String exportFileName(Q query) {
        return "export_" + System.currentTimeMillis() + ".xlsx";
    }

    /**
     * 导入模板文件名（含 {@code .xlsx} 后缀）。
     *
     * @param query 查询条件
     * @return 模板文件名
     */
    default String templateFileName(Q query) {
        return "import_template.xlsx";
    }
}
