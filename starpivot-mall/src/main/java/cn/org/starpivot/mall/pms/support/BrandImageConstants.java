package cn.org.starpivot.mall.pms.support;

/**
 * 品牌 Logo 上传常量
 */
public final class BrandImageConstants {

    public static final String CATEGORY = "brand";

    public static final String[] ALLOWED_CONTENT_TYPES = {
            "image/png", "image/jpeg", "image/gif", "image/webp"
    };

    /** 单张最大 2MB */
    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024;

    private BrandImageConstants() {
    }
}
