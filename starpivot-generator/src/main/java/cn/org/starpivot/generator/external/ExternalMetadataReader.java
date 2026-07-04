package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.generator.config.GenConfig;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.utils.GenUtils;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过外部 JDBC 读取表结构（按数据库方言）
 */
@Component
@RequiredArgsConstructor
public class ExternalMetadataReader {

    private final GenConfig genConfig;
    private final ExternalSessionDataSourceHolder dataSourceHolder;
    private final ExternalMetadataDialectRegistry dialectRegistry;

    public long countTables(ExternalGenSession session, String tableName, String tableComment) {
        ExternalMetadataDialect dialect = dialectRegistry.get(session.getConnection());
        DataSource ds = dataSourceHolder.getOrCreate(session);
        String schema = ExternalDataSourceFactory.resolveSchema(session.getConnection());
        try {
            StringBuilder sql = new StringBuilder(dialect.countTablesSql());
            List<Object> params = new ArrayList<>();
            params.add(schema);
            appendTableFilter(dialect, sql, params, tableName, tableComment);
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                bindParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new BizException("查询表数量失败：" + e.getMessage());
        }
        return 0;
    }

    public List<GenTable> listTables(ExternalGenSession session, String tableName, String tableComment,
            int pageNum, int pageSize) {
        ExternalMetadataDialect dialect = dialectRegistry.get(session.getConnection());
        DataSource ds = dataSourceHolder.getOrCreate(session);
        String schema = ExternalDataSourceFactory.resolveSchema(session.getConnection());
        try {
            StringBuilder sql = new StringBuilder(dialect.listTablesSql());
            List<Object> params = new ArrayList<>();
            params.add(schema);
            appendTableFilter(dialect, sql, params, tableName, tableComment);
            appendPagination(dialect, sql, params, pageNum, pageSize);

            List<GenTable> list = new ArrayList<>();
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                bindParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GenTable table = new GenTable();
                        table.setTableName(rs.getString("table_name"));
                        table.setTableComment(rs.getString("table_comment"));
                        table.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
                        table.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
                        list.add(table);
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new BizException("查询表列表失败：" + e.getMessage());
        }
    }

    public GenTable loadTableWithColumns(ExternalGenSession session, String tableName, GenPathProfile pathProfile) {
        ExternalMetadataDialect dialect = dialectRegistry.get(session.getConnection());
        DataSource ds = dataSourceHolder.getOrCreate(session);
        String schema = ExternalDataSourceFactory.resolveSchema(session.getConnection());
        GenTable table = loadTableMeta(dialect, ds, schema, tableName);
        List<GenTableColumn> columns = loadColumns(dialect, ds, schema, tableName);
        table.setColumns(columns);
        String operName = SecurityContextUtils.getUsername();
        GenUtils.initTable(table, operName, genConfig);
        if (pathProfile != null && StringUtils.isNotEmpty(pathProfile.getBasePackage())) {
            table.setPackageName(pathProfile.getBasePackage());
            table.setModuleName(GenUtils.getModuleName(pathProfile.getBasePackage()));
        }
        for (GenTableColumn column : columns) {
            GenUtils.initColumnField(column, table);
        }
        return table;
    }

    private GenTable loadTableMeta(ExternalMetadataDialect dialect, DataSource ds, String schema, String tableName) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(dialect.tableMetaSql())) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new BizException("表不存在：" + tableName);
                }
                GenTable table = new GenTable();
                table.setTableName(rs.getString("table_name"));
                table.setTableComment(rs.getString("table_comment"));
                table.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
                table.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
                return table;
            }
        } catch (SQLException e) {
            throw new BizException("查询表信息失败：" + e.getMessage());
        }
    }

    private List<GenTableColumn> loadColumns(ExternalMetadataDialect dialect, DataSource ds, String schema,
            String tableName) {
        List<GenTableColumn> columns = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(dialect.columnsSql())) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GenTableColumn column = new GenTableColumn();
                    column.setColumnName(rs.getString("column_name"));
                    column.setIsRequired(rs.getString("is_required"));
                    column.setIsPk(rs.getString("is_pk"));
                    column.setSort(rs.getInt("sort"));
                    column.setColumnComment(rs.getString("column_comment"));
                    column.setIsIncrement(rs.getString("is_increment"));
                    column.setColumnType(rs.getString("column_type"));
                    columns.add(column);
                }
            }
        } catch (SQLException e) {
            throw new BizException("查询表字段失败：" + e.getMessage());
        }
        return columns;
    }

    private static void appendPagination(ExternalMetadataDialect dialect, StringBuilder sql, List<Object> params,
            int pageNum, int pageSize) {
        if (dialect.useOffsetFetch()) {
            sql.append(" ORDER BY ").append(dialect.orderByColumn()).append(" DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            params.add((long) (pageNum - 1) * pageSize);
            params.add(pageSize);
        } else {
            sql.append(" ORDER BY ").append(dialect.orderByColumn()).append(" DESC LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add((long) (pageNum - 1) * pageSize);
        }
    }

    private static void appendTableFilter(ExternalMetadataDialect dialect, StringBuilder sql, List<Object> params,
            String tableName, String tableComment) {
        if (StringUtils.isNotEmpty(tableName)) {
            sql.append(" AND LOWER(").append(dialect.tableNameFilterColumn()).append(") LIKE LOWER(?)");
            params.add("%" + tableName + "%");
        }
        if (StringUtils.isNotEmpty(tableComment)) {
            sql.append(" AND LOWER(").append(dialect.tableCommentFilterColumn()).append(") LIKE LOWER(?)");
            params.add("%" + tableComment + "%");
        }
    }

    private static void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}

