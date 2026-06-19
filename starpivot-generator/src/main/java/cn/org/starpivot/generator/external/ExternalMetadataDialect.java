package cn.org.starpivot.generator.external;

/**
 * 外部库元数据 SQL 方言
 */
public interface ExternalMetadataDialect {

    String countTablesSql();

    String listTablesSql();

    String tableMetaSql();

    String columnsSql();

    /** 分页占位符：MySQL/PG 为 LIMIT ? OFFSET ?；Oracle/SQL Server 为 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY */
    boolean useOffsetFetch();

    /** ORDER BY 列（含表别名） */
    String orderByColumn();

    /** WHERE 中表名列表达式 */
    String tableNameFilterColumn();

    /** WHERE 中表注释列表达式 */
    String tableCommentFilterColumn();
}

