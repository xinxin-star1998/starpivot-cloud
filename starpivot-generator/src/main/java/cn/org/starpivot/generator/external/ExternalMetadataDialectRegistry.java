package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 外部库元数据 SQL 方言注册表，按数据库类型路由到具体 {@link ExternalMetadataDialect} 实现。
 * <p>
 * {@link Component}：注册为 Spring Bean，构造时注入各数据库方言实现。
 */
@Component
public class ExternalMetadataDialectRegistry {

    private final Map<String, ExternalMetadataDialect> dialectMap;

    /**
     * 构造方言映射表，统一别名（如 postgres/postgresql、mssql/sqlserver）。
     *
     * @param mysql      MySQL 方言
     * @param postgresql PostgreSQL 方言
     * @param oracle     Oracle 方言
     * @param sqlServer  SQL Server 方言
     */
    public ExternalMetadataDialectRegistry(MySqlMetadataDialect mysql,
            PostgreSqlMetadataDialect postgresql,
            OracleMetadataDialect oracle,
            SqlServerMetadataDialect sqlServer) {
        dialectMap = Map.of(
                "mysql", mysql,
                "postgresql", postgresql,
                "postgres", postgresql,
                "oracle", oracle,
                "sqlserver", sqlServer,
                "mssql", sqlServer);
    }

    /**
     * 根据连接信息获取对应元数据方言。
     *
     * @param conn 外部库连接配置
     * @return 匹配的方言实现
     * @throws BizException 数据库类型不受支持时抛出
     */
    public ExternalMetadataDialect get(ExternalDbConnection conn) {
        String dbType = ExternalDataSourceFactory.normalizeDbType(conn.getDbType());
        ExternalMetadataDialect dialect = dialectMap.get(dbType);
        if (dialect == null) {
            throw new BizException("暂不支持的数据库类型：" + dbType);
        }
        return dialect;
    }
}
