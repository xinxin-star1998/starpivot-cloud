package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.dto.external.ExternalDownloadRequest;
import cn.org.starpivot.generator.domain.dto.external.ExternalImportRequest;
import cn.org.starpivot.generator.domain.dto.external.ExternalImportResultVO;
import cn.org.starpivot.generator.domain.dto.external.ExternalPreviewRequest;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.external.ExternalGenImportHelper;
import cn.org.starpivot.generator.external.ExternalGenTableHelper;
import cn.org.starpivot.generator.external.GenTableCodegenHelper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.utils.CharsetKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 外部库代码生成：预览、ZIP 下载与导入本地 gen_table。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGenCodegenSupport {

    private final ExternalGenSessionSupport sessionSupport;
    private final ExternalGenDraftSupport draftSupport;
    private final ExternalGenImportHelper importHelper;
    private final GenTableMapper genTableMapper;

    public Map<String, String> preview(ExternalPreviewRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        ExternalGenScope scope = sessionSupport.resolveScope(request.getGenScope());
        GenTable table = draftSupport.resolveTableForCodegen(session, request.getTableName());
        GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
        return GenTableCodegenHelper.preview(table, profile, scope, sessionSupport.resolveTemplateDir(session));
    }

    public byte[] download(ExternalDownloadRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        ExternalGenScope scope = sessionSupport.resolveScope(request.getGenScope());
        if (request.getTableNames().size() > sessionSupport.properties().getMaxTablesPerDownload()) {
            throw new BizException("单次最多下载 " + sessionSupport.properties().getMaxTablesPerDownload() + " 张表");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String tableName : request.getTableNames()) {
                GenTable table = draftSupport.resolveTableForCodegen(session, tableName);
                GenPathProfile profile = ExternalGenTableHelper.resolveEffectiveProfile(table, session.getPathProfile());
                GenTableCodegenHelper.writeZip(table, profile, zip, scope, sessionSupport.resolveTemplateDir(session));
            }
            writeReadme(zip, session, request.getTableNames(), scope);
        } catch (IOException e) {
            log.error("外部库代码 ZIP 生成失败", e);
            throw new BizException("生成代码失败：" + e.getMessage());
        }
        return outputStream.toByteArray();
    }

    public ExternalImportResultVO importToGenTable(ExternalImportRequest request) {
        ExternalGenSession session = sessionSupport.requireSession(request.getSessionId());
        if (session.getPathProfile() == null) {
            throw new BizException("请先完成路径与模板配置");
        }
        List<String> imported = new ArrayList<>();
        List<String> updated = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (String tableName : request.getTableNames()) {
            GenTable draft = draftSupport.resolveTableForCodegen(session, tableName);
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
}
