package cn.org.starpivot.generator.external;

import org.springframework.stereotype.Component;

/**
 * Oracle 外部库元数据 SQL 方言实现。
 * <p>
 * {@link Component}：注册为 Spring Bean，供 {@link ExternalMetadataDialectRegistry} 注入；
 * 实现 {@link ExternalMetadataDialect}，基于 {@code all_tables}、{@code all_tab_columns} 等视图查询元数据。
 */
@Component
public class OracleMetadataDialect implements ExternalMetadataDialect {

    /** {@inheritDoc} */
    @Override
    public String countTablesSql() {
        return """
                SELECT COUNT(*) FROM all_tables t
                LEFT JOIN all_tab_comments c
                  ON c.owner = t.owner AND c.table_name = t.table_name AND c.table_type = 'TABLE'
                WHERE t.owner = ?
                AND t.table_name NOT LIKE 'QRTZ%'
                AND t.table_name NOT LIKE 'GEN%'
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String listTablesSql() {
        return """
                SELECT t.table_name, c.comments AS table_comment, t.created AS create_time, t.last_ddl_time AS update_time
                FROM all_tables t
                LEFT JOIN all_tab_comments c
                  ON c.owner = t.owner AND c.table_name = t.table_name AND c.table_type = 'TABLE'
                WHERE t.owner = ?
                AND t.table_name NOT LIKE 'QRTZ%'
                AND t.table_name NOT LIKE 'GEN%'
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String tableMetaSql() {
        return """
                SELECT t.table_name, c.comments AS table_comment, t.created AS create_time, t.last_ddl_time AS update_time
                FROM all_tables t
                LEFT JOIN all_tab_comments c
                  ON c.owner = t.owner AND c.table_name = t.table_name AND c.table_type = 'TABLE'
                WHERE t.owner = ? AND t.table_name = ?
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String columnsSql() {
        return """
                SELECT col.column_name,
                       (CASE WHEN col.nullable = 'N' AND pk.column_name IS NULL THEN '1' ELSE '0' END) AS is_required,
                       (CASE WHEN pk.column_name IS NOT NULL THEN '1' ELSE '0' END) AS is_pk,
                       col.column_id AS sort,
                       comm.comments AS column_comment,
                       '0' AS is_increment,
                       (col.data_type || CASE WHEN col.data_length IS NOT NULL THEN '(' || col.data_length || ')' ELSE '' END) AS column_type
                FROM all_tab_columns col
                LEFT JOIN all_col_comments comm
                  ON comm.owner = col.owner AND comm.table_name = col.table_name AND comm.column_name = col.column_name
                LEFT JOIN (
                    SELECT acc.owner, acc.table_name, acc.column_name
                    FROM all_cons_columns acc
                    JOIN all_constraints ac ON ac.owner = acc.owner AND ac.constraint_name = acc.constraint_name
                    WHERE ac.constraint_type = 'P'
                ) pk ON pk.owner = col.owner AND pk.table_name = col.table_name AND pk.column_name = col.column_name
                WHERE col.owner = ? AND col.table_name = ?
                ORDER BY col.column_id
                """;
    }

    /** {@inheritDoc} */
    @Override
    public boolean useOffsetFetch() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String orderByColumn() {
        return "t.created";
    }

    /** {@inheritDoc} */
    @Override
    public String tableNameFilterColumn() {
        return "t.table_name";
    }

    /** {@inheritDoc} */
    @Override
    public String tableCommentFilterColumn() {
        return "c.comments";
    }
}

