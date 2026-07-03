package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.dto.external.*;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.external.*;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部库代码生成写盘、diff 预览与回退。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGenWriteSupport {

    private final ExternalGenSessionSupport sessionSupport;
    private final ExternalGenDraftSupport draftSupport;
    private final ExternalWriteDiffCache writeDiffCache;

    public ExternalWriteResultVO writeToDisk(ExternalWriteRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        ExternalGenScope scope = sessionSupport.resolveScope(request.getGenScope());
        if (request.getTableNames().size() > sessionSupport.properties().getMaxTablesPerDownload()) {
            throw new BizException("单次最多生成 " + sessionSupport.properties().getMaxTablesPerDownload() + " 张表");
        }
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), sessionSupport.properties());
        List<String> allWritten = new ArrayList<>();
        String mergedBackupId = null;
        int mergedBackedUp = 0;
        String templateDir = sessionSupport.resolveTemplateDir(session);
        boolean backupBeforeWrite = resolveBackupBeforeWrite(request.getBackupBeforeWrite());
        GenTableCodegenHelper.WriteDiskFilter filter = GenTableCodegenHelper.buildWriteFilter(
                request.isOnlyChanged(), request.getSelectedPaths(), backupBeforeWrite);
        for (String tableName : request.getTableNames()) {
            GenTable table = draftSupport.resolveTableForCodegen(session, tableName);
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

    public ExternalWriteRollbackResultVO rollbackWrite(ExternalWriteRollbackRequest request) {
        sessionSupport.requireSession(request.getSessionId());
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), sessionSupport.properties());
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

    public ExternalWriteDirListVO listWriteDirs(String path) {
        sessionSupport.assertEnabled();
        if (!sessionSupport.properties().isWriteToDiskEnabled()) {
            throw new BizException("服务端写盘生成功能未启用");
        }
        return ExternalWriteDirBrowser.listDirectories(path, sessionSupport.properties());
    }

    public ExternalWriteDiffResultVO previewWriteDiff(ExternalWriteDiffRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        ExternalGenScope scope = sessionSupport.resolveScope(request.getGenScope());
        if (request.getTableNames().size() > sessionSupport.properties().getMaxTablesPerDownload()) {
            throw new BizException("单次最多预览 " + sessionSupport.properties().getMaxTablesPerDownload() + " 张表");
        }
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), sessionSupport.properties());
        String templateDir = sessionSupport.resolveTemplateDir(session);
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

    public ExternalWriteDiffItemVO previewWriteDiffFile(ExternalWriteDiffFileRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        ExternalGenScope scope = sessionSupport.resolveScope(request.getGenScope());
        java.nio.file.Path outputRoot = ExternalWritePathValidator.resolveOutputRoot(
                request.getOutputRoot(), sessionSupport.properties());
        String templateDir = sessionSupport.resolveTemplateDir(session);
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

    public void invalidateDiffCache(String sessionId) {
        writeDiffCache.invalidate(sessionId);
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
            GenTable table = draftSupport.resolveTableForCodegen(session, tableName);
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

    private boolean resolveBackupBeforeWrite(Boolean requestValue) {
        if (requestValue != null) {
            return requestValue;
        }
        return sessionSupport.properties().isBackupBeforeWrite();
    }
}
