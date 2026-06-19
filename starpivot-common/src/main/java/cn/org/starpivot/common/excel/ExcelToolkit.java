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
 */
public final class ExcelToolkit {

    private ExcelToolkit() {}

    // ---------- 底层读写 ----------

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

    public static <T> byte[] templateBytes(String sheetName, Class<T> clazz) throws IOException {
        return exportBytes(sheetName, clazz, Collections.emptyList());
    }

    public static <T> List<T> parseImport(MultipartFile file, Class<T> clazz) throws IOException {
        return EasyExcel.read(file.getInputStream()).head(clazz).sheet().doReadSync();
    }

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

    public static ResponseEntity<?> emptyDataError() {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error("没有可导出的数据"));
    }

    public static ResponseEntity<?> exportError(Exception e) {
        return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error("导出失败：" + e.getMessage()));
    }

    // ---------- 结合 ExcelBizHandler（Controller 一行委托） ----------

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
