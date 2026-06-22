package cn.org.starpivot.generator.external;

import org.springframework.stereotype.Component;

/**
 * PostgreSQL 外部库元数据 SQL 方言实现。
 * <p>
 * {@link Component}：注册为 Spring Bean，供 {@link ExternalMetadataDialectRegistry} 注入；
 * 实现 {@link ExternalMetadataDialect}，基于 {@code information_schema} 查询表与列信息。
 */
@Component
public class PostgreSqlMetadataDialect implements ExternalMetadataDialect {

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
                       (CASE WHEN (is_nullable = 'NO' AND column_name NOT IN (
                           SELECT kcu.column_name FROM information_schema.table_constraints tc
                           JOIN information_schema.key_column_usage kcu
                             ON tc.constraint_name = kcu.constraint_name
                            AND tc.table_schema = kcu.table_schema
                           WHERE tc.constraint_type = 'PRIMARY KEY'
                             AND tc.table_schema = columns.table_schema
                             AND tc.table_name = columns.table_name
                       )) THEN '1' ELSE '0' END) AS is_required,
                       (CASE WHEN column_name IN (
                           SELECT kcu.column_name FROM information_schema.table_constraints tc
                           JOIN information_schema.key_column_usage kcu
                             ON tc.constraint_name = kcu.constraint_name
                            AND tc.table_schema = kcu.table_schema
                           WHERE tc.constraint_type = 'PRIMARY KEY'
                             AND tc.table_schema = columns.table_schema
                             AND tc.table_name = columns.table_name
                       ) THEN '1' ELSE '0' END) AS is_pk,
                       ordinal_position AS sort,
                       '' AS column_comment,
                       (CASE WHEN column_default LIKE 'nextval%' THEN '1' ELSE '0' END) AS is_increment,
                       udt_name AS column_type
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

