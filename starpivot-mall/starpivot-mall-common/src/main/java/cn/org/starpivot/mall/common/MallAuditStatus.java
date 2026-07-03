package cn.org.starpivot.mall.common;

/**
 * 商城业务单据审批状态（与 SAS 完结结果对齐）。
 */
public final class MallAuditStatus {

    public static final String DRAFT = "DRAFT";
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String WITHDRAWN = "WITHDRAWN";

    private MallAuditStatus() {
    }

    public static boolean canSubmit(String status) {
        return status == null
                || DRAFT.equals(status)
                || REJECTED.equals(status)
                || WITHDRAWN.equals(status);
    }

    public static boolean isApproved(String status) {
        return APPROVED.equals(status);
    }
}
