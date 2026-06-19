package cn.org.starpivot.generator.path;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;

import java.util.function.Function;

/**
 * 代码生成模板产物类型（路径后缀匹配，按 {@link #matchOrder()} 从具体到泛化）
 */
public enum GenTemplateArtifact {

    SUB_DOMAIN("vm/java/sub-domain.java.vm", GenConstants.TPL_SUB,
            ctx -> ctx.entityPackagePath(ctx.getSubClassName()),
            ctx -> ctx.getSubClassName() + ".java"),

    SUB_DTO("vm/java/dto/sub-dto.java.vm", GenConstants.TPL_SUB,
            ctx -> ctx.dtoPackagePath(ctx.getSubClassName()),
            ctx -> ctx.getSubClassName() + "DTO.java"),

    SUB_VO("vm/java/bo/sub-vo.java.vm", GenConstants.TPL_SUB,
            ctx -> ctx.voPackagePath(ctx.getSubClassName()),
            ctx -> ctx.getSubClassName() + "VO.java"),

    ENTITY("vm/java/domain.java.vm", null,
            ctx -> ctx.entityPackagePath(ctx.getClassName()),
            ctx -> ctx.getClassName() + ".java"),

    REQ_BO("vm/java/bo/reqBo.java.vm", null,
            GenPathContext::reqBoPackagePath,
            ctx -> ctx.getClassName() + ctx.reqBoSuffix() + ".java"),

    DTO("vm/java/dto/dto.java.vm", null,
            ctx -> ctx.dtoPackagePath(ctx.getClassName()),
            ctx -> ctx.getClassName() + "DTO.java"),

    VO("vm/java/bo/vo.java.vm", null,
            ctx -> ctx.voPackagePath(ctx.getClassName()),
            ctx -> ctx.getClassName() + "VO.java"),

    MAPPER("vm/java/mapper.java.vm", null,
            GenPathContext::mapperJavaPath,
            ctx -> ctx.getClassName() + "Mapper.java"),

    SERVICE_IMPL("vm/java/serviceImpl.java.vm", null,
            GenPathContext::serviceImplJavaPath,
            ctx -> ctx.getClassName() + "ServiceImpl.java"),

    SERVICE("vm/java/service.java.vm", null,
            GenPathContext::serviceJavaPath,
            ctx -> "I" + ctx.getClassName() + "Service.java"),

    CONTROLLER("vm/java/controller.java.vm", null,
            GenPathContext::controllerJavaPath,
            ctx -> ctx.getClassName() + "Controller.java"),

    MAPPER_XML("vm/xml/mapper.xml.vm", null,
            GenPathContext::mapperXmlZipPath,
            ctx -> ctx.getClassName() + "Mapper.xml"),

    SQL("vm/sql/sql.vm", null,
            GenPathContext::sqlZipPath,
            ctx -> ctx.getBusinessName() + "Menu.sql"),

    API_JS("vm/js/api.js.vm", null,
            ctx -> ctx.apiZipPath(ctx.getBusinessName() + ".js"),
            ctx -> ctx.getBusinessName() + ".js"),

    API_TS("vm/ts/api.ts.vm", null,
            ctx -> ctx.apiZipPath(ctx.getBusinessName() + ".ts"),
            ctx -> ctx.getBusinessName() + ".ts"),

    SEARCH_VUE("/modules/search.vue.vm", null,
            GenPathContext::searchVueZipPath,
            ctx -> ctx.getBusinessName() + "-search.vue"),

    DIALOG_VUE("/modules/dialog.vue.vm", null,
            GenPathContext::dialogVueZipPath,
            ctx -> ctx.getBusinessName() + "-dialog.vue"),

    INDEX_TREE("/index-tree.vue.vm", null,
            GenPathContext::indexVueZipPath,
            ctx -> "index.vue"),

    INDEX("/index.vue.vm", null,
            GenPathContext::indexVueZipPath,
            ctx -> "index.vue");

    private static final GenTemplateArtifact[] MATCH_ORDER = {
            SUB_DOMAIN, SUB_DTO, SUB_VO,
            SERVICE_IMPL, SERVICE,
            ENTITY, REQ_BO, DTO, VO,
            MAPPER, CONTROLLER, MAPPER_XML, SQL,
            API_JS, API_TS,
            SEARCH_VUE, DIALOG_VUE, INDEX_TREE, INDEX
    };

    private final String pathSuffix;
    private final String requiredTplCategory;
    private final Function<GenPathContext, String> zipPathBuilder;
    private final Function<GenPathContext, String> displayNameBuilder;

    GenTemplateArtifact(String pathSuffix, String requiredTplCategory,
            Function<GenPathContext, String> zipPathBuilder,
            Function<GenPathContext, String> displayNameBuilder) {
        this.pathSuffix = pathSuffix;
        this.requiredTplCategory = requiredTplCategory;
        this.zipPathBuilder = zipPathBuilder;
        this.displayNameBuilder = displayNameBuilder;
    }

    public static GenTemplateArtifact[] matchOrder() {
        return MATCH_ORDER;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }

    public boolean matches(String template) {
        return template != null && template.endsWith(pathSuffix);
    }

    public void validateForTable(GenTable table) {
        if (requiredTplCategory != null && !requiredTplCategory.equals(table.getTplCategory())) {
            throw new BizException("模板 " + pathSuffix + " 仅适用于 " + requiredTplCategory
                    + "，当前：" + table.getTplCategory());
        }
    }

    public String buildZipPath(GenPathContext ctx) {
        return zipPathBuilder.apply(ctx);
    }

    public String buildDisplayName(GenPathContext ctx) {
        return displayNameBuilder.apply(ctx);
    }
}

