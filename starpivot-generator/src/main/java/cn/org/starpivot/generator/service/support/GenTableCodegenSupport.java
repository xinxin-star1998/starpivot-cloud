package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.external.GenTableCodegenHelper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 内置（库内）代码生成：预览与 ZIP 下载。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenTableCodegenSupport {

    private final GenTableMapper genTableMapper;
    private final GenTableMetadataSupport metadataSupport;

    public Map<String, String> previewCode(Long tableId) {
        GenTable table = genTableMapper.selectGenTableById(tableId);
        metadataSupport.prepareForCodegen(table);
        return GenTableCodegenHelper.renderPrepared(table);
    }

    public byte[] downloadCode(String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            writeTableToZip(tableName, zip);
        } catch (IOException e) {
            log.error("生成代码压缩包失败，表名: {}", tableName, e);
            throw new BizException("生成代码失败：" + e.getMessage());
        }
        return outputStream.toByteArray();
    }

    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String tableName : tableNames) {
                writeTableToZip(tableName, zip);
            }
        } catch (IOException e) {
            log.error("批量生成代码压缩包失败", e);
            throw new BizException("批量生成代码失败");
        }
        return outputStream.toByteArray();
    }

    private void writeTableToZip(String tableName, ZipOutputStream zip) throws IOException {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        if (StringUtils.isNull(table)) {
            throw new BizException("生成代码失败，代码生成表不存在");
        }
        metadataSupport.prepareForCodegen(table);
        GenTableCodegenHelper.writeZipPrepared(table, zip);
    }
}
