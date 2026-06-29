package cn.org.starpivot.api.file;

import lombok.Getter;

/**
 * 文件中心业务分类枚举。
 */
@Getter
public enum FileCategory {

    SYSTEM("SYSTEM", "系统通用", "file/system/", 1L),
    OA("OA", "办公审批", "file/oa/", 2L),
    CONTRACT("CONTRACT", "合同档案", "file/contract/", 3L),
    CERT("CERT", "资质证件", "file/cert/", 4L),
    PROJECT("PROJECT", "项目资料", "file/project/", 5L),
    CUSTOMER("CUSTOMER", "客户资料", "file/customer/", 6L),
    GOODS("GOODS", "商品素材", "file/goods/", 7L),
    FINANCE("FINANCE", "财务单据", "file/finance/", 8L),
    HR("HR", "人事档案", "file/hr/", 9L),
    OTHER("OTHER", "其他附件", "file/other/", 10L);

    private final String code;
    private final String label;
    private final String ossPrefix;
    private final Long defaultFolderId;

    FileCategory(String code, String label, String ossPrefix, Long defaultFolderId) {
        this.code = code;
        this.label = label;
        this.ossPrefix = ossPrefix;
        this.defaultFolderId = defaultFolderId;
    }

    /** OSS 对象路径段，如 {@code file/oa/} */
    public String getObjectPathSegment() {
        return ossPrefix;
    }

    /** Category 数据权限标识，如 {@code file:category:HR} */
    public String getAccessPermission() {
        return "file:category:" + code;
    }

    public static FileCategory of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("业务分类不能为空");
        }
        for (FileCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的业务分类: " + code);
    }
}
