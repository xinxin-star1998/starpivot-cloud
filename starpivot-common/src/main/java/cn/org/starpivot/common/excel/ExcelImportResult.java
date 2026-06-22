package cn.org.starpivot.common.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果，支持部分成功场景。
 * <p>
 * 由 {@link ExcelBizHandler#importRows(java.util.List, ExcelImportOptions)} 返回，
 * {@link ExcelToolkit#importFile} 据此组装 {@link cn.org.starpivot.common.domain.Result} 提示信息。
 * </p>
 */
public class ExcelImportResult {

    private int successCount;
    private int failCount;
    private List<String> errorMessages;

    /** 构造空结果（成功/失败计数均为 0） */
    public ExcelImportResult() {
        this.successCount = 0;
        this.failCount = 0;
        this.errorMessages = new ArrayList<>();
    }

    /**
     * 创建全部导入成功的结果。
     *
     * @param count 成功条数
     * @return 仅含成功计数的结果对象
     */
    public static ExcelImportResult allSuccess(int count) {
        ExcelImportResult r = new ExcelImportResult();
        r.successCount = count;
        return r;
    }

    /** @return 成功导入条数 */
    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    /** @return 失败条数 */
    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    /** @return 逐行错误信息列表 */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
    }

    /**
     * 追加一条错误描述（通常含行号与原因）。
     *
     * @param error 错误信息
     */
    public void addError(String error) {
        if (this.errorMessages == null) {
            this.errorMessages = new ArrayList<>();
        }
        this.errorMessages.add(error);
    }
}
