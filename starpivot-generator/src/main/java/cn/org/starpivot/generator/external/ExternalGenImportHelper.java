package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.mapper.GenTableColumnMapper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.utils.StringUtils;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 外部库草稿导入 gen_table
 */
@Component
@RequiredArgsConstructor
public class ExternalGenImportHelper {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;

    @Transactional
    public void importTable(GenTable draft, ExternalGenSession session, boolean overwrite) {
        String tableName = draft.getTableName();
        GenTable existing = genTableMapper.selectGenTableByName(tableName);
        String operName = SecurityContextUtils.getUsername();
        GenTable toSave = buildPersistTable(draft, session, operName);

        if (existing != null) {
            if (!overwrite) {
                throw new BizException("表 " + tableName + " 已存在于代码生成配置中，请勾选覆盖或先在主库删除");
            }
            toSave.setTableId(existing.getTableId());
            genTableMapper.updateGenTable(toSave);
            genTableColumnMapper.deleteGenTableColumnByIds(List.of(existing.getTableId()));
            insertColumns(toSave, draft.getColumns(), operName);
        } else {
            genTableMapper.insertGenTable(toSave);
            if (StringUtils.isNotEmpty(toSave.getOptions()) || StringUtils.isNotEmpty(toSave.getSubTableName())) {
                genTableMapper.updateGenTable(toSave);
            }
            insertColumns(toSave, draft.getColumns(), operName);
        }
    }

    private GenTable buildPersistTable(GenTable draft, ExternalGenSession session, String operName) {
        GenTable table = new GenTable();
        BeanUtils.copyProperties(draft, table, "tableId", "subTable", "pkColumn", "params", "columns");
        table.setSubTable(null);
        table.setPkColumn(null);
        table.setParams(null);
        table.setColumns(null);
        table.setCreateBy(operName);
        table.setGenType(StringUtils.isNotEmpty(draft.getGenType()) ? draft.getGenType() : "0");
        if (StringUtils.isEmpty(table.getOptions())) {
            ExternalGenTableHelper.syncOptions(table);
        }
        String db = session.getConnection() != null ? session.getConnection().getDatabase() : "";
        table.setRemark("导入自外部库 " + db);
        return table;
    }

    private void insertColumns(GenTable table, List<GenTableColumn> columns, String operName) {
        if (table.getTableId() == null || StringUtils.isEmpty(columns)) {
            throw new BizException("表 " + table.getTableName() + " 字段为空，无法导入");
        }
        for (GenTableColumn column : columns) {
            GenTableColumn col = new GenTableColumn();
            BeanUtils.copyProperties(column, col, "columnId");
            col.setTableId(table.getTableId());
            col.setCreateBy(operName);
            genTableColumnMapper.insertGenTableColumn(col);
        }
    }
}

