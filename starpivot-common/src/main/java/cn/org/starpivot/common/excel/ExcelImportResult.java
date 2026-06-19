package cn.org.starpivot.common.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果（支持部分成功）
 */
public class ExcelImportResult {

    private int successCount;
    private int failCount;
    private List<String> errorMessages;

    public ExcelImportResult() {
        this.successCount = 0;
        this.failCount = 0;
        this.errorMessages = new ArrayList<>();
    }

    public static ExcelImportResult allSuccess(int count) {
        ExcelImportResult r = new ExcelImportResult();
        r.successCount = count;
        return r;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
    }

    public void addError(String error) {
        if (this.errorMessages == null) {
            this.errorMessages = new ArrayList<>();
        }
        this.errorMessages.add(error);
    }
}
