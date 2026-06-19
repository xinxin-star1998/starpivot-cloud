package cn.org.starpivot.generator.path;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.GenPathProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成模板目录与路径解析（模板列表与 {@link GenTemplateArtifact} 注册表同源维护）
 */
public final class GenTemplateRegistry {

    private static final List<String> CORE_BACKEND_TEMPLATES = List.of(
            "vm/java/domain.java.vm",
            "vm/java/bo/reqBo.java.vm",
            "vm/java/dto/dto.java.vm",
            "vm/java/bo/vo.java.vm",
            "vm/java/mapper.java.vm",
            "vm/java/service.java.vm",
            "vm/java/serviceImpl.java.vm",
            "vm/java/controller.java.vm",
            "vm/xml/mapper.xml.vm",
            "vm/sql/sql.vm");

    private static final List<String> SUB_TABLE_TEMPLATES = List.of(
            "vm/java/sub-domain.java.vm",
            "vm/java/dto/sub-dto.java.vm",
            "vm/java/bo/sub-vo.java.vm");

    private static final List<String> TPL_CATEGORIES = List.of(
            GenConstants.TPL_CRUD,
            GenConstants.TPL_TREE,
            GenConstants.TPL_SUB);

    /** 与 {@link #resolveWebTypeDir} 支持的前端类型保持一致 */
    private static final List<String> WEB_TYPES = List.of(
            "art-design-pro",
            "vue2",
            "element-ui",
            "vue3",
            "element-plus");

    private GenTemplateRegistry() {
    }

    public static GenTemplateArtifact match(String template) {
        for (GenTemplateArtifact artifact : GenTemplateArtifact.matchOrder()) {
            if (artifact.matches(template)) {
                return artifact;
            }
        }
        throw new BizException("未注册的代码生成模板：" + template);
    }

    public static String resolveZipEntryPath(String template, GenTable table, GenPathProfile profile) {
        GenTemplateArtifact artifact = match(template);
        artifact.validateForTable(table);
        return artifact.buildZipPath(GenPathContext.of(table, profile));
    }

    public static String resolveDisplayName(String template, GenTable table) {
        GenTemplateArtifact artifact = match(template);
        return artifact.buildDisplayName(GenPathContext.of(table, null));
    }

    /**
     * 按模板类型与前端框架返回 Velocity 模板路径列表
     */
    public static List<String> getTemplateList(String tplCategory, String tplWebType) {
        String useWebType = resolveWebTypeDir(tplWebType);
        List<String> templates = new ArrayList<>(CORE_BACKEND_TEMPLATES);
        templates.add(resolveApiTemplate(tplWebType));
        // Element UI (vue2) 仅存在 index/index-tree 单文件模板，无 modules 子目录
        boolean isVue2 = isVue2WebType(tplWebType);
        if (GenConstants.TPL_CRUD.equals(tplCategory)) {
            templates.add(useWebType + "/index.vue.vm");
            if (!isVue2) {
                templates.add(useWebType + "/modules/search.vue.vm");
                templates.add(useWebType + "/modules/dialog.vue.vm");
            }
        } else if (GenConstants.TPL_TREE.equals(tplCategory)) {
            templates.add(useWebType + "/index-tree.vue.vm");
            if (!isVue2) {
                templates.add(useWebType + "/modules/search.vue.vm");
                templates.add(useWebType + "/modules/dialog.vue.vm");
            }
        } else if (GenConstants.TPL_SUB.equals(tplCategory)) {
            templates.add(useWebType + "/index.vue.vm");
            if (!isVue2) {
                templates.add(useWebType + "/modules/search.vue.vm");
                templates.add(useWebType + "/modules/dialog.vue.vm");
            }
            templates.addAll(SUB_TABLE_TEMPLATES);
        }
        return templates;
    }

    /**
     * 校验指定组合的模板列表均已注册路径规则
     */
    public static void validateTemplateListCoverage(String tplCategory, String tplWebType) {
        for (String template : getTemplateList(tplCategory, tplWebType)) {
            match(template);
        }
    }

    /**
     * 校验所有支持的 tplCategory × tplWebType 组合均已注册路径规则
     */
    public static void validateAllTemplateListCoverage() {
        for (String tplCategory : TPL_CATEGORIES) {
            for (String tplWebType : WEB_TYPES) {
                validateTemplateListCoverage(tplCategory, tplWebType);
            }
        }
    }

    static String resolveWebTypeDir(String tplWebType) {
        if (isVue2WebType(tplWebType)) {
            return "vm/vue/v2";
        }
        if ("vue3".equals(tplWebType) || "element-plus".equals(tplWebType)) {
            return "vm/vue/v3";
        }
        return "vm/vue/art-design-pro";
    }

    static String resolveApiTemplate(String tplWebType) {
        return isVue2WebType(tplWebType) ? "vm/js/api.js.vm" : "vm/ts/api.ts.vm";
    }

    static boolean isVue2WebType(String tplWebType) {
        return "vue2".equals(tplWebType) || "element-ui".equals(tplWebType);
    }
}

