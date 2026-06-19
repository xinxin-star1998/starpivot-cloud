package cn.org.starpivot.generator.service.impl;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenConfig;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.dto.external.*;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.external.*;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.service.GenExternalService;
import cn.org.starpivot.generator.utils.CharsetKit;
import cn.org.starpivot.generator.utils.GenUtils;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenExternalServiceImpl implements GenExternalService {

    private final GenExternalProperties externalProperties;
    private final GenConfig genConfig;
    private final ExternalGenSessionStore sessionStore;
    private final ExternalMetadataReader metadataReader;
    private final ExternalSessionDataSourceHolder dataSourceHolder;
    private final ExternalGenImportHelper importHelper;
    private final GenTableMapper genTableMapper;
    private final ExternalWriteDiffCache writeDiffCache;

    @Override
    public ExternalConnectResultVO connect(ExternalDbConnection connection) {
        assertEnabled();
        ExternalDataSourceFactory.assertAllowedHost(connection, externalProperties);
        String sessionId = "ext_" + UUID.randomUUID().toString().replace("-", "");
        ExternalGenSession session = new ExternalGenSession();
        session.setSessionId(sessionId);
        session.setConnection(connection);

        DataSource ds = dataSourceHolder.register(sessionId, connection);
        try {
            if (!ds.getConnection().isValid(5)) {
                throw new BizException("数据库连接无效");
            }
        } catch (Exception e) {
            dataSourceHolder.remove(sessionId);
            throw new BizException("数据库连接失败：" + e.getMessage());
        }
        String version = ExternalDataSourceFactory.queryDbVersion(ds, connection);
        session.setDbVersion(version);
        sessionStore.save(session);

        return ExternalConnectResultVO.builder()
                .sessionId(sessionId)
                .database(connection.getDatabase())
                .dbVersion(version)
                .expireMinutes(externalProperties.getSessionTtlMinutes())
                .build();
    }

    @Override
    public void disconnect(String sessionId) {
        dataSourceHolder.remove(sessionId);
        writeDiffCache.invalidate(sessionId);
        sessionStore.remove(sessionId);
    }

    @Override
    public ExternalSessionStatusVO getSessionStatus(String sessionId) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(sessionId);
        long remaining = 0;
        if (session.getExpireAt() != null) {
            remaining = Math.max(0, Duration.between(Instant.now(), session.getExpireAt()).getSeconds());
        }
        return ExternalSessionStatusVO.builder()
                .sessionId(sessionId)
                .database(session.getConnection().getDatabase())
                .dbVersion(session.getDbVersion())
                .remainingSeconds(remaining)
                .expireMinutes(externalProperties.getSessionTtlMinutes())
                .templateDir(session.getTemplateDir())
                .effectiveTemplateDir(resolveTemplateDir(session))
                .build();
    }

    @Override
    public PageResponse<GenTable> listTables(ExternalTableQueryDTO query) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(query.getSessionId());
        long total = metadataReader.countTables(session, query.getTableName(), query.getTableComment());
        List<GenTable> rows = metadataReader.listTables(
                session,
                query.getTableName(),
                query.getTableComment(),
                query.getPageNum(),
                query.getPageSize());
        PageResponse<GenTable> page = new PageResponse<>();
        page.setTotal(total);
        page.setRows(rows);
        page.setPageNum((long) query.getPageNum());
        page.setPageSize((long) query.getPageSize());
        long pageCount = query.getPageSize() > 0 ? (total + query.getPageSize() - 1) / query.getPageSize() : 0;
        page.setPageCount(pageCount);
        return page;
    }

    @Override
    public List<GenTableColumn> listColumns(ExternalColumnsRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        GenTable draft = metadataReader.loadTableWithColumns(
                session, request.getTableName(), session.getPathProfile());
        session.getTableDrafts().put(request.getTableName(), draft);
        sessionStore.save(session);
        return draft.getColumns();
    }

    @Override
    public void savePathProfile(ExternalPathProfileRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        GenPathProfile profile = request.getPathProfile();
        String module = GenUtils.getModuleName(profile.getBasePackage());
        profile.fillDefaults(module);
        profile.validate();
        session.setPathProfile(profile);
        writeDiffCache.invalidate(session.getSessionId());
        sessionStore.save(session);
    }

    @Override
    public void saveDraft(ExternalDraftRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
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
        sessionStore.save(session);
    }

    @Override
    public void saveGenConfig(ExternalGenConfigRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
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
        sessionStore.save(session);
    }

    @Override
    public void saveTemplateDir(ExternalTemplateDirRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        String dir = request.getTemplateDir();
        if (StringUtils.isEmpty(dir)) {
            session.setTemplateDir(null);
        } else {
            ExternalWritePathValidator.assertTemplateDir(dir.trim());
            session.setTemplateDir(dir.trim());
        }
        sessionStore.save(session);
    }

    @Override
    public Map<String, String> preview(ExternalPreviewRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        GenTable table = resolveTableForCodegen(session, request.getTableName());
        GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
        return GenTableCodegenHelper.preview(table, profile, scope, resolveTemplateDir(session));
    }

    @Override
    public byte[] download(ExternalDownloadRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        if (request.getTableNames().size() > externalProperties.getMaxTablesPerDownload()) {
            throw new BizException("单次最多下载 " + externalProperties.getMaxTablesPerDownload() + " 张表");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String tableName : request.getTableNames()) {
                GenTable table = resolveTableForCodegen(session, tableName);
                GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
                GenTableCodegenHelper.writeZip(table, profile, zip, scope, resolveTemplateDir(session));
            }
            writeReadme(zip, session, request.getTableNames(), scope);
        } catch (IOException e) {
            log.error("外部库代码 ZIP 生成失败", e);
            throw new BizException("生成代码失败：" + e.getMessage());
        }
        return outputStream.toByteArray();
    }

    @Override
    public ExternalImportResultVO importToGenTable(ExternalImportRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        if (session.getPathProfile() == null) {
            throw new BizException("请先完成路径与模板配置");
        }
        List<String> imported = new ArrayList<>();
        List<String> updated = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (String tableName : request.getTableNames()) {
            GenTable draft = resolveTableForCodegen(session, tableName);
            GenTable existing = genTableMapper.selectGenTableByName(tableName);
            if (existing != null && !request.isOverwrite()) {
                skipped.add(tableName);
                continue;
            }
            importHelper.importTable(draft, session, request.isOverwrite());
            if (existing != null) {
                updated.add(tableName);
            } else {
                imported.add(tableName);
            }
        }
        return ExternalImportResultVO.builder()
                .imported(imported)
                .updated(updated)
                .skipped(skipped)
                .build();
    }

    @Override
    public ExternalWriteResultVO writeToDisk(ExternalWriteRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        if (request.getTableNames().size() > externalProperties.getMaxTablesPerDownload()) {
            throw new BizException("单次最多生成 " + externalProperties.getMaxTablesPerDownload() + " 张表");
        }
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), externalProperties);
        List<String> allWritten = new ArrayList<>();
        String mergedBackupId = null;
        int mergedBackedUp = 0;
        String templateDir = resolveTemplateDir(session);
        boolean backupBeforeWrite = resolveBackupBeforeWrite(request.getBackupBeforeWrite());
        GenTableCodegenHelper.WriteDiskFilter filter = GenTableCodegenHelper.buildWriteFilter(
                request.isOnlyChanged(), request.getSelectedPaths(), backupBeforeWrite);
        for (String tableName : request.getTableNames()) {
            GenTable table = resolveTableForCodegen(session, tableName);
            GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
            try {
                GenTableCodegenHelper.WriteToDiskOutcome outcome = GenTableCodegenHelper.writeToDiskOutcome(
                        table, profile, scope, outputRoot, templateDir, filter);
                allWritten.addAll(outcome.writtenFiles());
                if (StringUtils.isNotEmpty(outcome.backupId())) {
                    mergedBackupId = outcome.backupId();
                    mergedBackedUp += outcome.backedUpCount();
                }
            } catch (IOException e) {
                log.error("写盘生成失败，表: {}", tableName, e);
                throw new BizException("写盘生成失败：" + e.getMessage());
            }
        }
        if (allWritten.isEmpty()) {
            throw new BizException("没有符合写入条件的文件");
        }
        if (mergedBackedUp == 0) {
            mergedBackupId = null;
        }
        writeDiffCache.invalidate(request.getSessionId());
        return ExternalWriteResultVO.builder()
                .outputRoot(outputRoot.toString())
                .writtenFiles(allWritten)
                .fileCount(allWritten.size())
                .backupId(mergedBackupId)
                .backedUpCount(mergedBackedUp)
                .build();
    }

    @Override
    public ExternalWriteRollbackResultVO rollbackWrite(ExternalWriteRollbackRequest request) {
        assertEnabled();
        sessionStore.getRequired(request.getSessionId());
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), externalProperties);
        try {
            List<String> restored = ExternalWriteBackupHelper.rollback(outputRoot, request.getBackupId());
            return ExternalWriteRollbackResultVO.builder()
                    .backupId(request.getBackupId())
                    .outputRoot(outputRoot.toString())
                    .restoredFiles(restored)
                    .fileCount(restored.size())
                    .build();
        } catch (IOException e) {
            log.error("写盘回退失败，backupId: {}", request.getBackupId(), e);
            throw new BizException("写盘回退失败：" + e.getMessage());
        }
    }

    @Override
    public ExternalWriteDirListVO listWriteDirs(String path) {
        assertEnabled();
        if (!externalProperties.isWriteToDiskEnabled()) {
            throw new BizException("服务端写盘生成功能未启用");
        }
        return ExternalWriteDirBrowser.listDirectories(path, externalProperties);
    }

    private boolean resolveBackupBeforeWrite(Boolean requestValue) {
        if (requestValue != null) {
            return requestValue;
        }
        return externalProperties.isBackupBeforeWrite();
    }

    @Override
    public ExternalWriteDiffResultVO previewWriteDiff(ExternalWriteDiffRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        if (request.getTableNames().size() > externalProperties.getMaxTablesPerDownload()) {
            throw new BizException("单次最多预览 " + externalProperties.getMaxTablesPerDownload() + " 张表");
        }
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), externalProperties);
        String templateDir = resolveTemplateDir(session);
        String cacheKey = writeDiffCache.buildCacheKey(
                request.getTableNames(), scope, outputRoot, templateDir, session.getPathProfile());
        List<ExternalWriteDiffItemVO> merged = getOrComputeWriteDiff(
                session, request.getTableNames(), scope, outputRoot, templateDir, cacheKey);
        List<ExternalWriteDiffItemVO> responseItems = merged;
        if (Boolean.FALSE.equals(request.getIncludeContent())) {
            responseItems = ExternalWriteDiffCache.summaryOnly(merged);
        }
        return buildWriteDiffResult(outputRoot, responseItems);
    }

    @Override
    public ExternalWriteDiffItemVO previewWriteDiffFile(ExternalWriteDiffFileRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), externalProperties);
        String templateDir = resolveTemplateDir(session);
        String cacheKey = writeDiffCache.buildCacheKey(
                request.getTableNames(), scope, outputRoot, templateDir, session.getPathProfile());
        if (!writeDiffCache.matches(request.getSessionId(), cacheKey)) {
            getOrComputeWriteDiff(session, request.getTableNames(), scope, outputRoot, templateDir, cacheKey);
        }
        ExternalWriteDiffItemVO item = writeDiffCache.getFile(request.getSessionId(), request.getPath());
        if (item == null) {
            throw new BizException("文件不在本次生成范围内：" + request.getPath());
        }
        return item;
    }

    private List<ExternalWriteDiffItemVO> getOrComputeWriteDiff(ExternalGenSession session, List<String> tableNames,
            ExternalGenScope scope, java.nio.file.Path outputRoot, String templateDir, String cacheKey) {
        if (writeDiffCache.matches(session.getSessionId(), cacheKey)) {
            return writeDiffCache.list(session.getSessionId());
        }
        List<ExternalWriteDiffItemVO> merged = computeWriteDiff(session, tableNames, scope, outputRoot, templateDir);
        writeDiffCache.put(session.getSessionId(), cacheKey, merged);
        return merged;
    }

    private List<ExternalWriteDiffItemVO> computeWriteDiff(ExternalGenSession session, List<String> tableNames,
            ExternalGenScope scope, java.nio.file.Path outputRoot, String templateDir) {
        Map<String, ExternalWriteDiffItemVO> byPath = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            GenTable table = resolveTableForCodegen(session, tableName);
            GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
            try {
                for (ExternalWriteDiffItemVO item : GenTableCodegenHelper.computeWriteDiff(
                        table, profile, scope, outputRoot, templateDir)) {
                    if (byPath.containsKey(item.getPath())) {
                        log.warn("多表写盘 diff 路径冲突，后者覆盖: {}", item.getPath());
                    }
                    byPath.put(item.getPath(), item);
                }
            } catch (IOException e) {
                log.error("写盘 diff 预览失败，表: {}", tableName, e);
                throw new BizException("写盘 diff 预览失败：" + e.getMessage());
            }
        }
        return new ArrayList<>(byPath.values());
    }

    private ExternalWriteDiffResultVO buildWriteDiffResult(java.nio.file.Path outputRoot,
            List<ExternalWriteDiffItemVO> merged) {
        int newCount = 0;
        int modifiedCount = 0;
        int unchangedCount = 0;
        for (ExternalWriteDiffItemVO item : merged) {
            switch (item.getStatus()) {
                case GenTableCodegenHelper.DIFF_NEW -> newCount++;
                case GenTableCodegenHelper.DIFF_MODIFIED -> modifiedCount++;
                case GenTableCodegenHelper.DIFF_UNCHANGED -> unchangedCount++;
                default -> {
                }
            }
        }
        return ExternalWriteDiffResultVO.builder()
                .outputRoot(outputRoot.toString())
                .newCount(newCount)
                .modifiedCount(modifiedCount)
                .unchangedCount(unchangedCount)
                .files(merged)
                .build();
    }

    @Override
    public Map<String, Object> listTemplates() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", List.of(
                Map.of("value", "crud", "label", "单表（增删改查）"),
                Map.of("value", "tree", "label", "树表（增删改查）"),
                Map.of("value", "sub", "label", "主子表（增删改查）")));
        result.put("webTypes", List.of(
                Map.of("value", "art-design-pro", "label", "Vue3 Art Design Pro"),
                Map.of("value", "element-plus", "label", "Vue3 Element Plus"),
                Map.of("value", "element-ui", "label", "Vue2 Element UI")));
        return result;
    }

    @Override
    public Map<String, Object> listCapabilities() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dbTypes", List.of(
                Map.of("value", "mysql", "label", "MySQL", "defaultPort", 3306),
                Map.of("value", "postgresql", "label", "PostgreSQL", "defaultPort", 5432),
                Map.of("value", "oracle", "label", "Oracle", "defaultPort", 1521),
                Map.of("value", "sqlserver", "label", "SQL Server", "defaultPort", 1433)));
        result.put("oracleConnectModes", List.of(
                Map.of("value", "service", "label", "Service（含 RAC SCAN）"),
                Map.of("value", "sid", "label", "SID"),
                Map.of("value", "tns", "label", "TNS 描述符")));
        result.put("writeToDiskEnabled", externalProperties.isWriteToDiskEnabled());
        result.put("backupBeforeWrite", externalProperties.isBackupBeforeWrite());
        result.put("globalTemplateDir", externalProperties.getTemplateDir());
        result.put("defaultOutputRoot", externalProperties.getDefaultOutputRoot());
        result.put("maxTablesPerBatch", externalProperties.getMaxTablesPerDownload());
        return result;
    }

    private GenTable resolveTableForCodegen(ExternalGenSession session, String tableName) {
        GenPathProfile profile = session.getPathProfile();
        if (profile == null) {
            throw new BizException("请先配置生成路径（pathProfile）");
        }
        GenTable table = session.getTableDrafts().get(tableName);
        if (table == null) {
            table = metadataReader.loadTableWithColumns(session, tableName, profile);
            applyPathProfileToTable(table, profile);
            session.getTableDrafts().put(tableName, table);
            sessionStore.save(session);
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

    private void writeReadme(ZipOutputStream zip, ExternalGenSession session, List<String> tableNames,
            ExternalGenScope scope) throws IOException {
        GenPathProfile profile = session.getPathProfile();
        StringBuilder sb = new StringBuilder();
        sb.append("StarPivot 外部库代码生成包\n");
        sb.append("========================\n\n");
        sb.append("数据库: ").append(session.getConnection().getDatabase()).append('\n');
        sb.append("基础包: ").append(profile != null ? profile.getBasePackage() : "-").append('\n');
        if (scope != null && !scope.isAllEnabled()) {
            sb.append("生成范围: ");
            sb.append(scope.isGenBackend() ? "后端 " : "");
            sb.append(scope.isGenFrontend() ? "前端 " : "");
            sb.append(scope.isGenSql() ? "菜单SQL " : "");
            sb.append('\n');
        }
        sb.append("\n包含表:\n");
        for (String name : tableNames) {
            sb.append("  - ").append(name).append('\n');
        }
        sb.append("\n使用说明:\n");
        sb.append("1. Java 文件按 main/java/... 目录合并到对应模块\n");
        sb.append("2. Mapper XML 合并到 main/resources/mapper/...\n");
        sb.append("3. 前端 api/vue 文件位于 vue/ 目录，合并到 star-pivot-ui/src/ 对应位置\n");
        sb.append("4. 执行 *Menu.sql 导入菜单（注意 parentMenuId）\n");
        sb.append("5. 检查权限前缀、路由 path 与现有模块是否冲突\n");
        zip.putNextEntry(new ZipEntry("README.txt"));
        IOUtils.write(sb.toString(), zip, CharsetKit.CHARSET_UTF_8);
        zip.closeEntry();
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

    private void assertEnabled() {
        if (!externalProperties.isEnabled()) {
            throw new BizException("外部库代码生成功能未启用");
        }
    }

    private ExternalGenScope resolveScope(ExternalGenScope scope) {
        ExternalGenScope effective = scope != null ? scope : new ExternalGenScope();
        effective.validate();
        return effective;
    }

    private String resolveTemplateDir(ExternalGenSession session) {
        if (session != null && StringUtils.isNotEmpty(session.getTemplateDir())) {
            ExternalWritePathValidator.assertTemplateDir(session.getTemplateDir());
            return session.getTemplateDir().trim();
        }
        String dir = externalProperties.getTemplateDir();
        if (StringUtils.isEmpty(dir)) {
            return null;
        }
        ExternalWritePathValidator.assertTemplateDir(dir);
        return dir.trim();
    }
}

