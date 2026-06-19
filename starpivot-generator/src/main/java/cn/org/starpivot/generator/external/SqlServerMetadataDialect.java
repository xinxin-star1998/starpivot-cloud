package cn.org.starpivot.generator.external;

import org.springframework.stereotype.Component;

@Component
public class SqlServerMetadataDialect implements ExternalMetadataDialect {

    @Override
    public String countTablesSql() {
        return """
                SELECT COUNT(*) FROM sys.tables t
                INNER JOIN sys.schemas s ON t.schema_id = s.schema_id
                LEFT JOIN sys.extended_properties ep
                  ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description'
                WHERE s.name = ?
                AND t.name NOT LIKE 'qrtz%'
                AND t.name NOT LIKE 'gen%'
                """;
    }

    @Override
    public String listTablesSql() {
        return """
                SELECT t.name AS table_name,
                       CAST(ep.value AS NVARCHAR(500)) AS table_comment,
                       t.create_date AS create_time,
                       t.modify_date AS update_time
                FROM sys.tables t
                INNER JOIN sys.schemas s ON t.schema_id = s.schema_id
                LEFT JOIN sys.extended_properties ep
                  ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description'
                WHERE s.name = ?
                AND t.name NOT LIKE 'qrtz%'
                AND t.name NOT LIKE 'gen%'
                """;
    }

    @Override
    public String tableMetaSql() {
        return """
                SELECT t.name AS table_name,
                       CAST(ep.value AS NVARCHAR(500)) AS table_comment,
                       t.create_date AS create_time,
                       t.modify_date AS update_time
                FROM sys.tables t
                INNER JOIN sys.schemas s ON t.schema_id = s.schema_id
                LEFT JOIN sys.extended_properties ep
                  ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description'
                WHERE s.name = ? AND t.name = ?
                """;
    }

    @Override
    public String columnsSql() {
        return """
                SELECT c.COLUMN_NAME AS column_name,
                       (CASE WHEN c.IS_NULLABLE = 'NO' AND pk.COLUMN_NAME IS NULL THEN '1' ELSE '0' END) AS is_required,
                       (CASE WHEN pk.COLUMN_NAME IS NOT NULL THEN '1' ELSE '0' END) AS is_pk,
                       c.ORDINAL_POSITION AS sort,
                       CAST(col_ep.value AS NVARCHAR(500)) AS column_comment,
                       (CASE WHEN COLUMNPROPERTY(OBJECT_ID(QUOTENAME(c.TABLE_SCHEMA) + '.' + QUOTENAME(c.TABLE_NAME)), c.COLUMN_NAME, 'IsIdentity') = 1 THEN '1' ELSE '0' END) AS is_increment,
                       (c.DATA_TYPE + CASE WHEN c.CHARACTER_MAXIMUM_LENGTH IS NOT NULL
                           THEN '(' + CAST(c.CHARACTER_MAXIMUM_LENGTH AS VARCHAR(20)) + ')'
                           WHEN c.NUMERIC_PRECISION IS NOT NULL
                           THEN '(' + CAST(c.NUMERIC_PRECISION AS VARCHAR(20)) + CASE WHEN c.NUMERIC_SCALE IS NOT NULL THEN ',' + CAST(c.NUMERIC_SCALE AS VARCHAR(20)) ELSE '' END + ')'
                           ELSE '' END) AS column_type
                FROM INFORMATION_SCHEMA.COLUMNS c
                LEFT JOIN (
                    SELECT ku.TABLE_SCHEMA, ku.TABLE_NAME, ku.COLUMN_NAME
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                    JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE ku
                      ON tc.CONSTRAINT_NAME = ku.CONSTRAINT_NAME AND tc.TABLE_SCHEMA = ku.TABLE_SCHEMA
                    WHERE tc.CONSTRAINT_TYPE = 'PRIMARY KEY'
                ) pk ON pk.TABLE_SCHEMA = c.TABLE_SCHEMA AND pk.TABLE_NAME = c.TABLE_NAME AND pk.COLUMN_NAME = c.COLUMN_NAME
                LEFT JOIN sys.extended_properties col_ep
                  ON col_ep.major_id = OBJECT_ID(QUOTENAME(c.TABLE_SCHEMA) + '.' + QUOTENAME(c.TABLE_NAME))
                 AND col_ep.minor_id = c.ORDINAL_POSITION AND col_ep.name = 'MS_Description'
                WHERE c.TABLE_SCHEMA = ? AND c.TABLE_NAME = ?
                ORDER BY c.ORDINAL_POSITION
                """;
    }

    @Override
    public boolean useOffsetFetch() {
        return true;
    }

    @Override
    public String orderByColumn() {
        return "t.create_date";
    }

    @Override
    public String tableNameFilterColumn() {
        return "t.name";
    }

    @Override
    public String tableCommentFilterColumn() {
        return "CAST(ep.value AS NVARCHAR(500))";
    }
}

