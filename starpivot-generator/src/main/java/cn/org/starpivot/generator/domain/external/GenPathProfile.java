package cn.org.starpivot.generator.domain.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.utils.StringUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 代码生成路径配置（会话级，不落库）
 */
@Data
public class GenPathProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$");
    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9_./\\-]+$");

    /** ZIP 内 Java 源码根目录 */
    public static final String JAVA_SRC_ROOT = "main/java/";
    /** ZIP 内 MyBatis XML 目录前缀 */
    public static final String MAPPER_XML_DIR_PREFIX = "main/resources/mapper/";
    /** 内置下载 ZIP 的前端根目录 */
    public static final String BUILTIN_VUE_ROOT = "vue";
    /** 外部写盘默认 API 目录前缀 */
    public static final String EXTERNAL_API_DIR_PREFIX = "star-pivot-ui/src/api/";

    @NotBlank(message = "基础包名不能为空")
    private String basePackage;

    private String entityPackage;
    private String dtoPackage;
    private String voPackage;
    private String boPackage;

    private String mapperPackage;
    private String servicePackage;
    private String serviceImplPackage;
    private String controllerPackage;

    /** 写盘/API 预览：Mapper XML 相对路径，如 main/resources/mapper/mall */
    private String mapperXmlPath;

    /** 写盘/API 预览：API 相对路径，如 star-pivot-ui/src/api/mall */
    private String apiPath;

    /** 写盘/API 预览：页面相对路径，如 star-pivot-ui/src/views/mall/brand */
    private String vuePagePath;

    /** ZIP 内子组件目录，默认可由 {@link #resolveVueModulesPath()} 推导 */
    private String vueModulesPath;

    /**
     * 根据基础包与模块名填充默认子包路径（未填写的项）
     */
    public void fillDefaults(String moduleName) {
        if (StringUtils.isEmpty(basePackage)) {
            throw new BizException("基础包名 basePackage 不能为空");
        }
        String module = StringUtils.isNotEmpty(moduleName) ? moduleName : getModuleNameFromBase();
        if (StringUtils.isEmpty(entityPackage)) {
            entityPackage = basePackage + ".domain.entity";
        }
        if (StringUtils.isEmpty(dtoPackage)) {
            dtoPackage = basePackage + ".domain.dto";
        }
        if (StringUtils.isEmpty(voPackage)) {
            voPackage = basePackage + ".domain.bo";
        }
        if (StringUtils.isEmpty(boPackage)) {
            boPackage = basePackage + ".domain.bo";
        }
        if (StringUtils.isEmpty(mapperPackage)) {
            mapperPackage = basePackage + ".mapper";
        }
        if (StringUtils.isEmpty(servicePackage)) {
            servicePackage = basePackage + ".service";
        }
        if (StringUtils.isEmpty(serviceImplPackage)) {
            serviceImplPackage = basePackage + ".service.impl";
        }
        if (StringUtils.isEmpty(controllerPackage)) {
            controllerPackage = basePackage + ".controller";
        }
        if (StringUtils.isEmpty(mapperXmlPath)) {
            mapperXmlPath = MAPPER_XML_DIR_PREFIX + module;
        }
        if (StringUtils.isEmpty(apiPath)) {
            apiPath = EXTERNAL_API_DIR_PREFIX + module;
        }
    }

    /**
     * 库内（ZIP 下载）生成：Java 包走 {@link #fillDefaults}，前端路径使用 {@link #BUILTIN_VUE_ROOT} 布局
     */
    public static GenPathProfile forBuiltinTable(GenTable table) {
        if (StringUtils.isEmpty(table.getPackageName())) {
            throw new BizException("生成包名不能为空");
        }
        GenPathProfile profile = new GenPathProfile();
        profile.setBasePackage(table.getPackageName());
        profile.fillDefaults(table.getModuleName());
        applyBuiltinFrontendZipLayout(table, profile);
        return profile;
    }

    /**
     * 外部 ZIP 下载：保留 pathProfile 中的 Java 包配置，前端 ZIP 条目与库内下载一致（vue/ 布局）
     */
    public static GenPathProfile forZipExport(GenTable table, GenPathProfile source) {
        if (source == null) {
            return forBuiltinTable(table);
        }
        GenPathProfile profile = source.copy();
        if (StringUtils.isEmpty(profile.getBasePackage())) {
            profile.setBasePackage(table.getPackageName());
        }
        profile.fillDefaults(table.getModuleName());
        applyBuiltinFrontendZipLayout(table, profile);
        return profile;
    }

    static void applyBuiltinFrontendZipLayout(GenTable table, GenPathProfile profile) {
        profile.setApiPath(BUILTIN_VUE_ROOT + "/api/" + table.getModuleName());
        profile.setVuePagePath(null);
        profile.setVueModulesPath(BUILTIN_VUE_ROOT + "/views/" + table.getModuleName()
                + "/" + table.getBusinessName() + "/modules");
    }

    public String resolveVueModulesPath() {
        if (StringUtils.isNotEmpty(vueModulesPath)) {
            return vueModulesPath;
        }
        if (StringUtils.isNotEmpty(vuePagePath)) {
            return vuePagePath + "/modules";
        }
        return null;
    }

    public GenPathProfile copy() {
        GenPathProfile p = new GenPathProfile();
        p.setBasePackage(basePackage);
        p.setEntityPackage(entityPackage);
        p.setDtoPackage(dtoPackage);
        p.setVoPackage(voPackage);
        p.setBoPackage(boPackage);
        p.setMapperPackage(mapperPackage);
        p.setServicePackage(servicePackage);
        p.setServiceImplPackage(serviceImplPackage);
        p.setControllerPackage(controllerPackage);
        p.setMapperXmlPath(mapperXmlPath);
        p.setApiPath(apiPath);
        p.setVuePagePath(vuePagePath);
        p.setVueModulesPath(vueModulesPath);
        return p;
    }

    public void validate() {
        if (StringUtils.isEmpty(basePackage)) {
            throw new BizException("基础包名不能为空");
        }
        validatePackage(basePackage, "basePackage");
        validatePackage(entityPackage, "entityPackage");
        validatePackage(dtoPackage, "dtoPackage");
        validatePackage(voPackage, "voPackage");
        validatePackage(boPackage, "boPackage");
        validatePackage(mapperPackage, "mapperPackage");
        validatePackage(servicePackage, "servicePackage");
        validatePackage(serviceImplPackage, "serviceImplPackage");
        validatePackage(controllerPackage, "controllerPackage");
        validateZipPath(mapperXmlPath, "mapperXmlPath");
        validateZipPath(apiPath, "apiPath");
        if (StringUtils.isNotEmpty(vuePagePath)) {
            validateZipPath(vuePagePath, "vuePagePath");
        }
        if (StringUtils.isNotEmpty(vueModulesPath)) {
            validateZipPath(vueModulesPath, "vueModulesPath");
        }
    }

    private String getModuleNameFromBase() {
        int idx = basePackage.lastIndexOf('.');
        return idx >= 0 ? basePackage.substring(idx + 1) : basePackage;
    }

    private static void validatePackage(String pkg, String field) {
        if (StringUtils.isEmpty(pkg)) {
            return;
        }
        if (!PACKAGE_PATTERN.matcher(pkg).matches()) {
            throw new BizException("非法 Java 包名：" + field);
        }
    }

    private static void validateZipPath(String path, String field) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        if (path.contains("..") || path.startsWith("/") || path.contains(":")) {
            throw new BizException("非法路径（禁止 ..、绝对盘符）：" + field);
        }
        if (!SAFE_PATH_PATTERN.matcher(path).matches()) {
            throw new BizException("路径包含非法字符：" + field);
        }
    }
}

