package cn.org.starpivot.generator.external;

import org.springframework.stereotype.Component;

/**
 * MySQL 外部库元数据 SQL 方言实现。
 * <p>
 * {@link Component}：注册为 Spring Bean，供 {@link ExternalMetadataDialectRegistry} 注入；
 * 实现 {@link ExternalMetadataDialect}，基于 {@code information_schema} 查询表与列信息。
 */
@Component
public class MySqlMetadataDialect implements ExternalMetadataDialect {

    /** {@inheritDoc} */
    @Override
    public String countTablesSql() {
        return """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = ?
                AND table_name NOT LIKE 'qrtz%'
                AND table_name NOT LIKE 'gen%'
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String listTablesSql() {
        return """
                SELECT table_name, table_comment, create_time, update_time
                FROM information_schema.tables
                WHERE table_schema = ?
                AND table_name NOT LIKE 'qrtz%'
                AND table_name NOT LIKE 'gen%'
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String tableMetaSql() {
        return """
                SELECT table_name, table_comment, create_time, update_time
                FROM information_schema.tables
                WHERE table_schema = ? AND table_name = ?
                """;
    }

    /** {@inheritDoc} */
    @Override
    public String columnsSql() {
        return """
                SELECT column_name,
                       (CASE WHEN (is_nullable = 'no' AND column_key != 'PRI') THEN '1' ELSE '0' END) AS is_required,
                       (CASE WHEN column_key = 'PRI' THEN '1' ELSE '0' END) AS is_pk,
                       ordinal_position AS sort,
                       column_comment,
                       (CASE WHEN extra = 'auto_increment' THEN '1' ELSE '0' END) AS is_increment,
                       column_type
                FROM information_schema.columns
                WHERE table_schema = ? AND table_name = ?
                ORDER BY ordinal_position
                """;
    }

    /** {@inheritDoc} */
    @Override
    public boolean useOffsetFetch() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String orderByColumn() {
        return "create_time";
    }

    /** {@inheritDoc} */
    @Override
    public String tableNameFilterColumn() {
        return "table_name";
    }

    /** {@inheritDoc} */
    @Override
    public String tableCommentFilterColumn() {
        return "table_comment";
    }
}
