package cn.org.starpivot.generator.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.generator.domain.dto.external.*;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.external.ExternalMetadataReader;
import cn.org.starpivot.generator.service.GenExternalService;
import cn.org.starpivot.generator.service.support.ExternalGenCodegenSupport;
import cn.org.starpivot.generator.service.support.ExternalGenDraftSupport;
import cn.org.starpivot.generator.service.support.ExternalGenSessionSupport;
import cn.org.starpivot.generator.service.support.ExternalGenWriteSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link GenExternalService} 默认实现，编排外部库连接、元数据读取、代码生成与写盘全流程。
 */
@Service
@RequiredArgsConstructor
public class GenExternalServiceImpl implements GenExternalService {

    private final ExternalGenSessionSupport sessionSupport;
    private final ExternalGenDraftSupport draftSupport;
    private final ExternalGenCodegenSupport codegenSupport;
    private final ExternalGenWriteSupport writeSupport;
    private final ExternalMetadataReader metadataReader;

    @Override
    public ExternalConnectResultVO connect(ExternalDbConnection connection) {
        return sessionSupport.connect(connection);
    }

    @Override
    public void disconnect(String sessionId) {
        sessionSupport.disconnect(sessionId);
        writeSupport.invalidateDiffCache(sessionId);
    }

    @Override
    public ExternalSessionStatusVO getSessionStatus(String sessionId) {
        return sessionSupport.getSessionStatus(sessionId);
    }

    @Override
    public PageResponse<GenTable> listTables(ExternalTableQueryDTO query) {
        sessionSupport.assertEnabled();
        ExternalGenSession session = sessionSupport.requireSession(query.getSessionId());
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

    @Override
    public List<GenTableColumn> listColumns(ExternalColumnsRequest request) {
        sessionSupport.assertEnabled();
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        GenTable draft = metadataReader.loadTableWithColumns(
                session, request.getTableName(), session.getPathProfile());
        session.getTableDrafts().put(request.getTableName(), draft);
        sessionSupport.sessionStore().save(session);
        return draft.getColumns();
    }

    @Override
    public void savePathProfile(ExternalPathProfileRequest request) {
        draftSupport.savePathProfile(request);
    }

    @Override
    public void saveDraft(ExternalDraftRequest request) {
        draftSupport.saveDraft(request);
    }

    @Override
    public void saveGenConfig(ExternalGenConfigRequest request) {
        draftSupport.saveGenConfig(request);
    }

    @Override
    public void saveTemplateDir(ExternalTemplateDirRequest request) {
        draftSupport.saveTemplateDir(request);
    }

    @Override
    public Map<String, String> preview(ExternalPreviewRequest request) {
        return codegenSupport.preview(request);
    }

    @Override
    public byte[] download(ExternalDownloadRequest request) {
        return codegenSupport.download(request);
    }

    @Override
    public ExternalImportResultVO importToGenTable(ExternalImportRequest request) {
        return codegenSupport.importToGenTable(request);
    }

    @Override
    public ExternalWriteResultVO writeToDisk(ExternalWriteRequest request) {
        return writeSupport.writeToDisk(request);
    }

    @Override
    public ExternalWriteDiffResultVO previewWriteDiff(ExternalWriteDiffRequest request) {
        return writeSupport.previewWriteDiff(request);
    }

    @Override
    public ExternalWriteDiffItemVO previewWriteDiffFile(ExternalWriteDiffFileRequest request) {
        return writeSupport.previewWriteDiffFile(request);
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
    public ExternalWriteRollbackResultVO rollbackWrite(ExternalWriteRollbackRequest request) {
        return writeSupport.rollbackWrite(request);
    }

    @Override
    public ExternalWriteDirListVO listWriteDirs(String path) {
        return writeSupport.listWriteDirs(path);
    }

    @Override
    public Map<String, Object> listCapabilities() {
        var props = sessionSupport.properties();
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
        result.put("writeToDiskEnabled", props.isWriteToDiskEnabled());
        result.put("backupBeforeWrite", props.isBackupBeforeWrite());
        result.put("globalTemplateDir", props.getTemplateDir());
        result.put("defaultOutputRoot", props.getDefaultOutputRoot());
        result.put("maxTablesPerBatch", props.getMaxTablesPerDownload());
        return result;
    }
}
