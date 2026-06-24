package cn.org.starpivot.mall.pms.support;

/**
 * 商品图片上传常量
 */
public final class GoodsImageConstants {

    /** @deprecated 已迁移至文件中心路径 file/goods/，保留常量仅供文档引用 */
    @Deprecated
    public static final String CATEGORY = "goods";

    public static final String[] ALLOWED_CONTENT_TYPES = {
            "image/png", "image/jpeg", "image/gif", "image/webp"
    };

    /** 单张最大 5MB */
    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private GoodsImageConstants() {
    }
}
