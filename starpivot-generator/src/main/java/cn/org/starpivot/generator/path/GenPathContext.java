package cn.org.starpivot.generator.path;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.utils.StringUtils;
import org.apache.velocity.VelocityContext;

/**
 * 代码生成 ZIP 路径解析上下文（内置 / 外部 profile 统一）
 */
public final class GenPathContext {

    private final GenTable table;
    private final String entityPackage;
    private final String dtoPackage;
    private final String voPackage;
    private final String boPackage;
    private final String mapperPackage;
    private final String servicePackage;
    private final String serviceImplPackage;
    private final String controllerPackage;
    private final String mapperXmlPath;
    private final String apiPath;
    private final String vuePagePath;
    private final String vueModulesPath;

    private GenPathContext(GenTable table, GenPathProfile profile) {
        this.table = table;
        GenPathProfile resolved = profile != null ? profile.copy() : GenPathProfile.forBuiltinTable(table);
        if (profile != null) {
            resolved.fillDefaults(table.getModuleName());
        }
        entityPackage = resolved.getEntityPackage();
        dtoPackage = resolved.getDtoPackage();
        voPackage = resolved.getVoPackage();
        boPackage = resolved.getBoPackage();
        mapperPackage = resolved.getMapperPackage();
        servicePackage = resolved.getServicePackage();
        serviceImplPackage = resolved.getServiceImplPackage();
        controllerPackage = resolved.getControllerPackage();
        mapperXmlPath = resolved.getMapperXmlPath();
        apiPath = resolved.getApiPath();
        vuePagePath = resolved.getVuePagePath();
        vueModulesPath = resolved.resolveVueModulesPath();
    }

    public static GenPathContext of(GenTable table, GenPathProfile profile) {
        return new GenPathContext(table, profile);
    }

    public static String toJavaZipDir(String javaPackage) {
        return GenPathProfile.JAVA_SRC_ROOT + javaPackage.replace('.', '/');
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public String getDtoPackage() {
        return dtoPackage;
    }

    public String getVoPackage() {
        return voPackage;
    }

    public String getBoPackage() {
        return boPackage;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public String getServicePackage() {
        return servicePackage;
    }

    public String getServiceImplPackage() {
        return serviceImplPackage;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public String getMapperXmlPath() {
        return mapperXmlPath;
    }

    public String getApiPath() {
        return apiPath;
    }

    public String getVuePagePath() {
        return vuePagePath;
    }

    public String getVueModulesPath() {
        return vueModulesPath;
    }

    /** 注入 Velocity 模板路径变量，与 ZIP 路径解析保持一致 */
    public void applyToVelocityContext(VelocityContext context) {
        context.put("entityPackage", entityPackage);
        context.put("dtoPackage", dtoPackage);
        context.put("voPackage", voPackage);
        context.put("boPackage", boPackage);
        context.put("mapperPackage", mapperPackage);
        context.put("servicePackage", servicePackage);
        context.put("serviceImplPackage", serviceImplPackage);
        context.put("controllerPackage", controllerPackage);
        if (StringUtils.isNotEmpty(mapperXmlPath)) {
            context.put("mapperXmlPath", mapperXmlPath);
        }
        if (StringUtils.isNotEmpty(apiPath)) {
            context.put("apiPath", apiPath);
        }
        if (StringUtils.isNotEmpty(vuePagePath)) {
            context.put("vuePagePath", vuePagePath);
        }
        if (StringUtils.isNotEmpty(vueModulesPath)) {
            context.put("vueModulesPath", vueModulesPath);
        }
    }

    public GenTable getTable() {
        return table;
    }

    public String getClassName() {
        return table.getClassName();
    }

    public String getBusinessName() {
        return table.getBusinessName();
    }

    public String getModuleName() {
        return table.getModuleName();
    }

    public String getSubClassName() {
        requireSubTable();
        return table.getSubTable().getClassName();
    }

    public String reqBoSuffix() {
        return GenConstants.TPL_TREE.equals(table.getTplCategory()) ? "ReqBo" : "ReqPageBo";
    }

    public void requireSubTable() {
        if (StringUtils.isNull(table.getSubTable())) {
            throw new IllegalStateException("主子表模板缺少 subTable，表名：" + table.getTableName());
        }
    }

    public String javaPackagePath(String javaPackage) {
        return toJavaZipDir(javaPackage);
    }

    public String entityPackagePath(String className) {
        return javaPackagePath(entityPackage) + "/" + className + ".java";
    }

    public String dtoPackagePath(String className) {
        return javaPackagePath(dtoPackage) + "/" + className + "DTO.java";
    }

    public String voPackagePath(String className) {
        return javaPackagePath(voPackage) + "/" + className + "VO.java";
    }

    public String reqBoPackagePath() {
        return javaPackagePath(boPackage) + "/" + getClassName() + reqBoSuffix() + ".java";
    }

    public String mapperJavaPath() {
        return javaPackagePath(mapperPackage) + "/" + getClassName() + "Mapper.java";
    }

    public String serviceJavaPath() {
        return javaPackagePath(servicePackage) + "/I" + getClassName() + "Service.java";
    }

    public String serviceImplJavaPath() {
        return javaPackagePath(serviceImplPackage) + "/" + getClassName() + "ServiceImpl.java";
    }

    public String controllerJavaPath() {
        return javaPackagePath(controllerPackage) + "/" + getClassName() + "Controller.java";
    }

    public String mapperXmlZipPath() {
        return trimSlash(mapperXmlPath) + "/" + getClassName() + "Mapper.xml";
    }

    public String sqlZipPath() {
        return getBusinessName() + "Menu.sql";
    }

    public String apiZipPath(String fileName) {
        return trimSlash(apiPath) + "/" + fileName;
    }

    public String searchVueZipPath() {
        return resolveVueModulePath(getBusinessName() + "-search.vue");
    }

    public String dialogVueZipPath() {
        return resolveVueModulePath(getBusinessName() + "-dialog.vue");
    }

    public String indexVueZipPath() {
        if (StringUtils.isNotEmpty(vuePagePath)) {
            return trimSlash(vuePagePath) + "/index.vue";
        }
        return GenPathProfile.BUILTIN_VUE_ROOT + "/views/" + getModuleName() + "/" + getBusinessName() + "/index.vue";
    }

    private String resolveVueModulePath(String fileName) {
        if (StringUtils.isNotEmpty(vueModulesPath)) {
            return trimSlash(vueModulesPath) + "/" + fileName;
        }
        return GenPathProfile.BUILTIN_VUE_ROOT + "/views/" + getModuleName() + "/" + getBusinessName()
                + "/modules/" + fileName;
    }

    private static String trimSlash(String path) {
        if (path == null) {
            return "";
        }
        String p = path;
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        while (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
}

