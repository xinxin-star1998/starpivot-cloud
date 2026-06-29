package cn.org.starpivot.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件中心 Category 数据权限配置。
 * <p>
 * 受限分类需额外拥有 {@code file:category:{CODE}} 权限；
 * 拥有 {@code file:category:all} 或超级管理员可访问全部分类。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "file-center.category-access")
public class FileCategoryAccessProperties {

    /** 是否启用 Category 级数据权限 */
    private boolean enabled = true;

    /** 需单独授权的业务分类（大写编码） */
    private List<String> restrictedCategories = defaultRestricted();

    /** 可访问全部分类的权限标识 */
    private String allCategoriesPermission = "file:category:all";

    private static List<String> defaultRestricted() {
        List<String> list = new ArrayList<>();
        list.add("HR");
        list.add("FINANCE");
        list.add("CONTRACT");
        list.add("CERT");
        return list;
    }
}
