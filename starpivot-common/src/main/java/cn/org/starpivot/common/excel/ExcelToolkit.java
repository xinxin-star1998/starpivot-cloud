package cn.org.starpivot.common.excel;

import cn.org.starpivot.common.domain.Result;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * 通用 EasyExcel 导入导出工具：读写 Excel、HTTP 响应封装、结合 {@link ExcelBizHandler} 完成 Controller 薄封装。
 * <p>
 * 导出接口建议配合 {@link cn.org.starpivot.common.annotation.NoResponseWrapper} 直接返回
 * {@link ResponseEntity}。
 * </p>
 */
public final class ExcelToolkit {

    private ExcelToolkit() {}

    // ---------- 底层读写 ----------

    /**
     * 将行数据写入 Excel 并返回字节数组。
     *
     * @param sheetName 工作表名
     * @param clazz     行类型（EasyExcel 注解类）
     * @param rows      数据行
     * @return xlsx 文件字节
     */
    public static <T> byte[] exportBytes(String sheetName, Class<T> clazz, List<T> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EasyExcel.write(baos)
                .head(clazz)
                .autoCloseStream(false)
                .sheet(sheetName)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(rows);
        return baos.toByteArray();
    }

    /**
     * 生成仅含表头的空模板 Excel。
     *
     * @param sheetName 工作表名
     * @param clazz     行类型
     * @return xlsx 文件字节
     */
    public static <T> byte[] templateBytes(String sheetName, Class<T> clazz) throws IOException {
        return exportBytes(sheetName, clazz, Collections.emptyList());
    }

    /**
     * 解析上传的 Excel 文件为行对象列表。
     *
     * @param file  上传文件
     * @param clazz 行类型
     * @return 同步读取的行列表
     */
    public static <T> List<T> parseImport(MultipartFile file, Class<T> clazz) throws IOException {
        return EasyExcel.read(file.getInputStream()).head(clazz).sheet().doReadSync();
    }

    /**
     * 构建 Excel 文件下载 {@link ResponseEntity}，设置 Content-Disposition 与 MIME 类型。
     *
     * @param bytes    文件内容
     * @param filename 下载文件名
     */
    public static ResponseEntity<?> excelFileResponse(byte[] bytes, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /** @return 无可导出数据时的 400 JSON 错误响应 */
    public static ResponseEntity<?> emptyDataError() {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error("没有可导出的数据"));
    }

    /**
     * 导出异常时的 500 JSON 错误响应。
     *
     * @param e 捕获的异常
     */
    public static ResponseEntity<?> exportError(Exception e) {
        return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error("导出失败：" + e.getMessage()));
    }

    // ---------- 结合 ExcelBizHandler（Controller 一行委托） ----------

    /**
     * 按 {@link ExcelBizHandler} 查询并导出 Excel 文件。
     *
     * @param handler  业务处理器
     * @param query    导出条件
     * @param rowClass 行类型
     */
    public static <T, Q> ResponseEntity<?> export(
            ExcelBizHandler<T, Q> handler, Q query, Class<T> rowClass) {
        try {
            List<T> rows = handler.listForExport(query);
            if (rows == null || rows.isEmpty()) {
                return emptyDataError();
            }
            byte[] bytes = exportBytes(handler.sheetName(query), rowClass, rows);
            return excelFileResponse(bytes, handler.exportFileName(query));
        } catch (Exception e) {
            return exportError(e);
        }
    }

    /**
     * 下载导入模板（含可选示例行）。
     *
     * @param handler  业务处理器
     * @param query    查询条件
     * @param rowClass 行类型
     */
    public static <T, Q> ResponseEntity<?> downloadTemplate(
            ExcelBizHandler<T, Q> handler, Q query, Class<T> rowClass) {
        try {
            List<T> samples = handler.templateSampleRows(query);
            byte[] bytes =
                    samples == null || samples.isEmpty()
                            ? templateBytes(handler.sheetName(query), rowClass)
                            : exportBytes(handler.sheetName(query), rowClass, samples);
            return excelFileResponse(bytes, handler.templateFileName(query));
        } catch (Exception e) {
            return exportError(e);
        }
    }

    /**
     * 解析上传 Excel 并委托 {@link ExcelBizHandler#importRows} 落库。
     *
     * @param file     上传文件
     * @param handler  业务处理器
     * @param options  导入选项，{@code null} 时默认不覆盖更新
     * @param rowClass 行类型
     * @return 含 {@link ExcelImportResult} 的 {@link Result}
     */
    public static <T, Q> Result<ExcelImportResult> importFile(
            MultipartFile file,
            ExcelBizHandler<T, Q> handler,
            ExcelImportOptions options,
            Class<T> rowClass)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传 Excel 文件");
        }
        List<T> rows = parseImport(file, rowClass);
        ExcelImportResult result =
                handler.importRows(rows, options != null ? options : ExcelImportOptions.of(false));
        String message =
                String.format(
                        "导入完成：成功 %d 条，失败 %d 条",
                        result.getSuccessCount(), result.getFailCount());
        if (result.getFailCount() > 0
                && result.getErrorMessages() != null
                && !result.getErrorMessages().isEmpty()) {
            return Result.success(
                    message + "。错误详情：" + String.join("; ", result.getErrorMessages()), result);
        }
        return Result.success(message, result);
    }
}
