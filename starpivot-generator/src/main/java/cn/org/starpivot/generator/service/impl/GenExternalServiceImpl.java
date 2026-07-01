package cn.org.starpivot.generator.service.impl;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.entity.PageResponse;
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

/**
 * {@link GenExternalService} 默认实现，编排外部库连接、元数据读取、代码生成与写盘全流程。
 * <p>
 * {@link Slf4j}：启用日志；
 * {@link Service}：注册为 Spring 服务 Bean；
 * {@link RequiredArgsConstructor}：注入外部库相关配置与会话、元数据、写盘组件。
 */
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

    /**
     * 建立外部数据库连接并创建生成会话。
     *
     * @param connection 外部库连接参数
     * @return 含 sessionId、数据库版本与过期时间的连接结果
     * @throws BizException 功能未启用、主机不允许或连接失败时抛出
     */
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

    /**
     * 断开外部库连接并清理会话、数据源及写盘 diff 缓存。
     *
     * @param sessionId 外部生成会话 ID
     */
    @Override
    public void disconnect(String sessionId) {
        dataSourceHolder.remove(sessionId);
        writeDiffCache.invalidate(sessionId);
        sessionStore.remove(sessionId);
    }

    /**
     * 查询外部生成会话的存活状态与模板目录信息。
     *
     * @param sessionId 外部生成会话 ID
     * @return 会话状态，含剩余有效秒数与生效模板目录
     * @throws BizException 功能未启用或会话不存在/已过期时抛出
     */
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

    /**
     * 分页查询外部库中的物理表列表。
     *
     * @param query 会话 ID、表名/注释筛选及分页参数
     * @return 外部库 {@link GenTable} 分页结果
     * @throws BizException 功能未启用或会话无效时抛出
     */
    @Override
    public PageResponse<GenTable> listTables(ExternalTableQueryDTO query) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(query.getSessionId());
        long total = metadataReader.countTables(session, query.getTableName(), query.getTableComment());
        List<GenTable> rows = metadataReader.listTables(
                session,
                query.getTableName(),
                query.getTableComment(),
                Math.toIntExact(query.getPageNum()),
                Math.toIntExact(query.getPageSize()));
        PageResponse<GenTable> page = new PageResponse<>();
        page.setTotal(total);
        page.setRows(rows);
        page.setPageNum((long) query.getPageNum());
        page.setPageSize((long) query.getPageSize());
        long pageCount = query.getPageSize() > 0 ? (total + query.getPageSize() - 1) / query.getPageSize() : 0;
        page.setPageCount(pageCount);
        return page;
    }

    /**
     * 加载指定外部表的列元数据并缓存为会话草稿。
     *
     * @param request 会话 ID 与表名
     * @return 列配置 {@link GenTableColumn} 列表
     * @throws BizException 功能未启用或会话无效时抛出
     */
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

    /**
     * 保存会话级代码生成路径配置（包名、模块路径等）。
     *
     * @param request 会话 ID 与 {@link GenPathProfile} 路径配置
     * @throws BizException 功能未启用、会话无效或路径校验失败时抛出
     */
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

    /**
     * 保存单张外部表的生成草稿（表注释、列配置、路径覆盖等）。
     *
     * @param request 表名、列配置及可选的前端/API 路径覆盖
     * @throws BizException 功能未启用或会话无效时抛出
     */
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

    /**
     * 批量保存多张表的模板类型、作者及路径配置。
     *
     * @param request 表名列表、模板分类/前端类型及可选路径配置
     * @throws BizException 功能未启用、会话无效或路径校验失败时抛出
     */
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

    /**
     * 设置或清除会话级自定义 Velocity 模板目录。
     *
     * @param request 会话 ID 与模板目录路径（空值表示恢复全局默认）
     * @throws BizException 功能未启用、会话无效或目录校验失败时抛出
     */
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

    /**
     * 预览单张外部表的生成代码（不落盘）。
     *
     * @param request 会话 ID、表名及生成范围
     * @return 相对路径到生成内容的映射
     * @throws BizException 功能未启用、会话无效或生成配置不完整时抛出
     */
    @Override
    public Map<String, String> preview(ExternalPreviewRequest request) {
        assertEnabled();
        ExternalGenSession session = sessionStore.getRequired(request.getSessionId());
        ExternalGenScope scope = resolveScope(request.getGenScope());
        GenTable table = resolveTableForCodegen(session, request.getTableName());
        GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
        return GenTableCodegenHelper.preview(table, profile, scope, resolveTemplateDir(session));
    }

    /**
     * 将多张外部表的生成代码打包为 ZIP 字节数组下载。
     *
     * @param request 会话 ID、表名列表及生成范围
     * @return 含 README 说明文件的 ZIP 压缩包字节内容
     * @throws BizException 功能未启用、超出批量上限或压缩失败时抛出
     */
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

    /**
     * 将外部库生成草稿导入本地 {@code gen_table} 持久化表。
     *
     * @param request 会话 ID、表名列表及是否覆盖已有记录
     * @return 新增、更新与跳过的表名分类结果
     * @throws BizException 功能未启用、未配置路径或会话无效时抛出
     */
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

    /**
     * 将生成代码直接写入服务端指定目录。
     *
     * @param request 输出根目录、表名列表、生成范围及写盘过滤选项
     * @return 已写入文件列表、备份 ID 及备份文件数
     * @throws BizException 功能未启用、超出批量上限、无符合写入条件的文件或 IO 失败时抛出
     */
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

    /**
     * 按备份 ID 回退上次写盘操作，恢复被覆盖的文件。
     *
     * @param request 会话 ID、输出根目录与备份 ID
     * @return 已恢复文件列表及数量
     * @throws BizException 功能未启用、会话无效或回退 IO 失败时抛出
     */
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

    /**
     * 浏览服务端允许写盘的目录列表。
     *
     * @param path 相对或绝对目录路径，空值表示根目录
     * @return 子目录与路径元信息
     * @throws BizException 写盘功能未启用或路径不在白名单内时抛出
     */
    @Override
    public ExternalWriteDirListVO listWriteDirs(String path) {
        assertEnabled();
        if (!externalProperties.isWriteToDiskEnabled()) {
            throw new BizException("服务端写盘生成功能未启用");
        }
        return ExternalWriteDirBrowser.listDirectories(path, externalProperties);
    }

    /**
     * 解析写盘前是否备份的请求值，未指定时使用全局配置。
     *
     * @param requestValue 请求中的备份开关，可为 {@code null}
     * @return 是否在本次写盘前创建备份
     */
    private boolean resolveBackupBeforeWrite(Boolean requestValue) {
        if (requestValue != null) {
            return requestValue;
        }
        return externalProperties.isBackupBeforeWrite();
    }

    /**
     * 预览写盘 diff：对比生成内容与目标目录现有文件的差异。
     *
     * @param request 输出根目录、表名列表、生成范围及是否包含文件内容
     * @return 新增/修改/未变更文件统计及明细列表
     * @throws BizException 功能未启用、超出批量上限或 diff 计算失败时抛出
     */
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

    /**
     * 预览写盘 diff 中单个文件的详细差异内容。
     *
     * @param request 会话 ID、表名列表、目标文件路径及生成范围
     * @return 单文件 diff 明细（含生成内容与变更状态）
     * @throws BizException 功能未启用、文件不在本次生成范围内或 diff 未缓存时抛出
     */
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

    /**
     * 从缓存获取或重新计算写盘 diff 明细列表。
     *
     * @param session 外部生成会话
     * @param tableNames 待对比的表名列表
     * @param scope 生成范围（后端/前端/SQL）
     * @param outputRoot 写盘目标根目录
     * @param templateDir 生效的模板目录
     * @param cacheKey diff 缓存键
     * @return 合并后的 {@link ExternalWriteDiffItemVO} 列表
     */
    private List<ExternalWriteDiffItemVO> getOrComputeWriteDiff(ExternalGenSession session, List<String> tableNames,
            ExternalGenScope scope, java.nio.file.Path outputRoot, String templateDir, String cacheKey) {
        if (writeDiffCache.matches(session.getSessionId(), cacheKey)) {
            return writeDiffCache.list(session.getSessionId());
        }
        List<ExternalWriteDiffItemVO> merged = computeWriteDiff(session, tableNames, scope, outputRoot, templateDir);
        writeDiffCache.put(session.getSessionId(), cacheKey, merged);
        return merged;
    }

    /**
     * 逐表计算写盘 diff 并按相对路径合并（路径冲突时后者覆盖）。
     *
     * @param session 外部生成会话
     * @param tableNames 待对比的表名列表
     * @param scope 生成范围
     * @param outputRoot 写盘目标根目录
     * @param templateDir 生效的模板目录
     * @return 去重合并后的 diff 明细列表
     * @throws BizException 单表 diff 计算 IO 失败时抛出
     */
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

    /**
     * 汇总 diff 明细列表的新增/修改/未变更计数并封装为响应 VO。
     *
     * @param outputRoot 写盘目标根目录
     * @param merged 合并后的 diff 明细列表
     * @return 含统计数与文件列表的 {@link ExternalWriteDiffResultVO}
     */
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

    /**
     * 返回前端可选的代码模板分类与 Web 框架类型枚举。
     *
     * @return 含 {@code categories} 与 {@code webTypes} 键的选项映射
     */
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

    /**
     * 返回外部库生成功能的能力清单与默认配置项。
     *
     * @return 含支持的数据库类型、写盘开关、模板目录及批量上限等配置
     */
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

    /**
     * 解析并校验用于代码生成的 {@link GenTable} 草稿（含主子表挂载）。
     *
     * @param session 外部生成会话
     * @param tableName 目标表名
     * @return 已应用路径配置、同步选项并完成校验的生成表实体
     * @throws BizException 未配置路径、模板类型缺失或校验失败时抛出
     */
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

    /**
     * 为主子表模板加载关联子表并设置主键列引用。
     *
     * @param session 外部生成会话
     * @param table 主表生成表实体
     * @param profile 路径配置，用于初始化子表包名
     */
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

    /**
     * 将前端/API 路径覆盖写入表草稿的 params 扩展参数。
     *
     * @param draft 表草稿实体
     * @param vuePagePath Vue 页面路径覆盖，{@code null} 表示不修改
     * @param apiPath API 路径覆盖，{@code null} 表示不修改
     */
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

    /**
     * 向 ZIP 压缩包写入 README 说明文件，描述数据库、包名及合并指引。
     *
     * @param zip ZIP 输出流
     * @param session 外部生成会话
     * @param tableNames 本次打包的表名列表
     * @param scope 生成范围，用于说明包含的后端/前端/SQL 内容
     * @throws IOException 写入 ZIP 条目失败时抛出
     */
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

    /**
     * 将 {@link GenPathProfile} 中的基础包名同步至生成表的 packageName 与 moduleName。
     *
     * @param table 目标生成表实体
     * @param profile 路径配置，为 {@code null} 时跳过
     */
    private void applyPathProfileToTable(GenTable table, GenPathProfile profile) {
        if (profile == null) {
            return;
        }
        if (StringUtils.isNotEmpty(profile.getBasePackage())) {
            table.setPackageName(profile.getBasePackage());
            table.setModuleName(GenUtils.getModuleName(profile.getBasePackage()));
        }
    }

    /**
     * 断言外部库代码生成功能已在配置中启用。
     *
     * @throws BizException 功能未启用时抛出
     */
    private void assertEnabled() {
        if (!externalProperties.isEnabled()) {
            throw new BizException("外部库代码生成功能未启用");
        }
    }

    /**
     * 解析并校验生成范围，未指定时返回全量启用的默认范围。
     *
     * @param scope 请求中的生成范围，可为 {@code null}
     * @return 校验通过的 {@link ExternalGenScope} 实例
     * @throws BizException 范围参数非法时抛出
     */
    private ExternalGenScope resolveScope(ExternalGenScope scope) {
        ExternalGenScope effective = scope != null ? scope : new ExternalGenScope();
        effective.validate();
        return effective;
    }

    /**
     * 解析生效的 Velocity 模板目录：优先会话级配置，其次全局配置。
     *
     * @param session 外部生成会话
     * @return 校验通过的模板目录路径，均未配置时返回 {@code null}
     * @throws BizException 模板目录路径校验失败时抛出
     */
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

