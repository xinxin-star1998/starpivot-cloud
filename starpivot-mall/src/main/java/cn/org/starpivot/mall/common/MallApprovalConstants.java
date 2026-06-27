package cn.org.starpivot.mall.common;

/**
 * 审批中台业务键与类型常量。
 */
public final class MallApprovalConstants {

    public static final String BIZ_MODULE = "mall";
    public static final String BIZ_TYPE_PURCHASE = "purchase";
    public static final String BIZ_TYPE_RETURN = "return";

    private MallApprovalConstants() {
    }

    public static String purchaseBizKey(Long purchaseId) {
        return bizKey(BIZ_TYPE_PURCHASE, purchaseId);
    }

    public static String returnBizKey(Long returnId) {
        return bizKey(BIZ_TYPE_RETURN, returnId);
    }

    private static String bizKey(String bizType, Long id) {
        return BIZ_MODULE + ":" + bizType + ":" + id;
    }

    public static Long parsePurchaseId(String bizKey) {
        return parseBizId(bizKey, BIZ_TYPE_PURCHASE);
    }

    public static Long parseReturnId(String bizKey) {
        return parseBizId(bizKey, BIZ_TYPE_RETURN);
    }

    private static Long parseBizId(String bizKey, String expectedType) {
        if (bizKey == null || bizKey.isBlank()) {
            return null;
        }
        String[] parts = bizKey.split(":");
        if (parts.length < 3 || !expectedType.equals(parts[1])) {
            return null;
        }
        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
