package cn.org.starpivot.tms.constant;

public final class TmsConstants {

    private TmsConstants() {
    }

    public static final String BIZ_MODULE_MALL = "mall";
    public static final String BIZ_TYPE_ORDER = "order";

    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_EXCEPTION = "EXCEPTION";

    public static final String EVENT_SOURCE_SYSTEM = "SYSTEM";
    public static final String EVENT_SOURCE_KUAIDI100 = "KUAIDI100";

    public static final String CARRIER_STATUS_NORMAL = "0";
    public static final String CARRIER_STATUS_DISABLED = "1";

    public static final String RULE_TYPE_FIXED = "FIXED";
    public static final String RULE_TYPE_WEIGHT = "WEIGHT";

    public static final String DEFAULT_FLAG_YES = "1";
    public static final String DEFAULT_FLAG_NO = "0";

    public static String bizKey(Long orderId) {
        return BIZ_MODULE_MALL + ":" + BIZ_TYPE_ORDER + ":" + orderId;
    }
}
