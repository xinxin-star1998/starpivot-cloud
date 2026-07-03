package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenConfig;
import cn.org.starpivot.generator.domain.dto.external.ExternalDraftRequest;
import cn.org.starpivot.generator.domain.dto.external.ExternalGenConfigRequest;
import cn.org.starpivot.generator.domain.dto.external.ExternalPathProfileRequest;
import cn.org.starpivot.generator.domain.dto.external.ExternalTemplateDirRequest;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.external.*;
import cn.org.starpivot.generator.utils.GenUtils;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 外部库代码生成草稿与路径配置管理。
 */
@Component
@RequiredArgsConstructor
public class ExternalGenDraftSupport {

    private final ExternalGenSessionSupport sessionSupport;
    private final GenConfig genConfig;
    private final ExternalMetadataReader metadataReader;
    private final ExternalWriteDiffCache writeDiffCache;

    public void savePathProfile(ExternalPathProfileRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        GenPathProfile profile = request.getPathProfile();
        String module = GenUtils.getModuleName(profile.getBasePackage());
        profile.fillDefaults(module);
        profile.validate();
        session.setPathProfile(profile);
        writeDiffCache.invalidate(session.getSessionId());
        sessionSupport.sessionStore().save(session);
    }

    public void saveDraft(ExternalDraftRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        GenTable draft = session.getTableDrafts().get(request.getTableName());
        if (draft == null) {
            draft = metadataReader.loadTableWithColumns(
                    session, request.getTableName(), session.getPathProfile());
        }
        if (StringUtils.isNotEmpty(request.getTableComment())) {
            draft.setTableComment(request.getTableComment());
        }
        if (StringUtils.isNotEmpty(request.getClassName())) {
            draft.setClassName(request.getClassName());
        }
        if (StringUtils.isNotEmpty(request.getBusinessName())) {
            draft.setBusinessName(request.getBusinessName());
        }
        if (StringUtils.isNotEmpty(request.getFunctionName())) {
            draft.setFunctionName(request.getFunctionName());
        }
        if (StringUtils.isNotEmpty(request.getTreeCode())) {
            draft.setTreeCode(request.getTreeCode());
        }
        if (StringUtils.isNotEmpty(request.getTreeParentCode())) {
            draft.setTreeParentCode(request.getTreeParentCode());
        }
        if (StringUtils.isNotEmpty(request.getTreeName())) {
            draft.setTreeName(request.getTreeName());
        }
        if (request.getParentMenuId() != null) {
            draft.setParentMenuId(request.getParentMenuId());
        }
        if (request.getSubTableName() != null) {
            draft.setSubTableName(request.getSubTableName());
        }
        if (request.getSubTableFkName() != null) {
            draft.setSubTableFkName(request.getSubTableFkName());
        }
        applyTablePathOverrides(draft, request.getVuePagePath(), request.getApiPath());
        if (request.getColumns() != null && !request.getColumns().isEmpty()) {
            draft.setColumns(request.getColumns());
        }
        applyPathProfileToTable(draft, session.getPathProfile());
        ExternalGenTableHelper.syncOptions(draft);
        session.getTableDrafts().put(request.getTableName(), draft);
        sessionSupport.sessionStore().save(session);
    }

    public void saveGenConfig(ExternalGenConfigRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        if (request.getPathProfile() != null) {
            GenPathProfile profile = request.getPathProfile();
            String module = GenUtils.getModuleName(profile.getBasePackage());
            profile.fillDefaults(module);
            profile.validate();
            session.setPathProfile(profile);
            writeDiffCache.invalidate(session.getSessionId());
        }
        if (StringUtils.isNotEmpty(request.getAuthor())) {
            session.setAuthor(request.getAuthor());
        }
        String author = StringUtils.isNotEmpty(session.getAuthor()) ? session.getAuthor() : genConfig.getAuthor();
        for (String tableName : request.getTableNames()) {
            GenTable draft = session.getTableDrafts().get(tableName);
            if (draft == null) {
                draft = metadataReader.loadTableWithColumns(
                        session, tableName, session.getPathProfile());
            }
            draft.setTplCategory(request.getTplCategory());
            draft.setTplWebType(request.getTplWebType());
            draft.setFunctionAuthor(author);
            applyPathProfileToTable(draft, session.getPathProfile());
            ExternalGenTableHelper.syncOptions(draft);
            session.getTableDrafts().put(tableName, draft);
        }
        sessionSupport.sessionStore().save(session);
    }

    public void saveTemplateDir(ExternalTemplateDirRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        String dir = request.getTemplateDir();
        if (StringUtils.isEmpty(dir)) {
            session.setTemplateDir(null);
        } else {
            ExternalWritePathValidator.assertTemplateDir(dir.trim());
            session.setTemplateDir(dir.trim());
        }
        sessionSupport.sessionStore().save(session);
    }

    public GenTable resolveTableForCodegen(ExternalGenSession session, String tableName) {
        GenPathProfile profile = session.getPathProfile();
        if (profile == null) {
            throw new BizException("请先配置生成路径（pathProfile）");
        }
        GenTable table = session.getTableDrafts().get(tableName);
        if (table == null) {
            table = metadataReader.loadTableWithColumns(session, tableName, profile);
            applyPathProfileToTable(table, profile);
            session.getTableDrafts().put(tableName, table);
            sessionSupport.sessionStore().save(session);
        }
        if (StringUtils.isEmpty(table.getTplCategory()) || StringUtils.isEmpty(table.getTplWebType())) {
            throw new BizException("请先配置模板类型（tplCategory / tplWebType）");
        }
        applyPathProfileToTable(table, profile);
        ExternalGenTableHelper.syncOptions(table);
        ExternalGenTableHelper.validateGenConfig(table);
        if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
            attachSubTable(session, table, profile);
        }
        return table;
    }

    private void attachSubTable(ExternalGenSession session, GenTable table, GenPathProfile profile) {
        String subTableName = table.getSubTableName();
        if (StringUtils.isEmpty(subTableName)) {
            return;
        }
        GenTable subTable = session.getTableDrafts().get(subTableName);
        if (subTable == null) {
            subTable = metadataReader.loadTableWithColumns(session, subTableName, profile);
            applyPathProfileToTable(subTable, profile);
            session.getTableDrafts().put(subTableName, subTable);
        }
        GenTableCodegenHelper.setPkColumn(subTable);
        table.setSubTable(subTable);
    }

    private void applyTablePathOverrides(GenTable draft, String vuePagePath, String apiPath) {
        if (vuePagePath != null) {
            if (StringUtils.isEmpty(vuePagePath)) {
                draft.getParams().remove(ExternalGenTableHelper.PARAM_VUE_PAGE_PATH);
            } else {
                draft.getParams().put(ExternalGenTableHelper.PARAM_VUE_PAGE_PATH, vuePagePath);
            }
        }
        if (apiPath != null) {
            if (StringUtils.isEmpty(apiPath)) {
                draft.getParams().remove(ExternalGenTableHelper.PARAM_API_PATH);
            } else {
                draft.getParams().put(ExternalGenTableHelper.PARAM_API_PATH, apiPath);
            }
        }
    }

    private void applyPathProfileToTable(GenTable table, GenPathProfile profile) {
        if (profile == null) {
            return;
        }
        if (StringUtils.isNotEmpty(profile.getBasePackage())) {
            table.setPackageName(profile.getBasePackage());
            table.setModuleName(GenUtils.getModuleName(profile.getBasePackage()));
        }
    }
}
