package cn.org.starpivot.generator.external;

import com.alibaba.druid.pool.DruidDataSource;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.utils.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 外部库 JDBC 连接工厂（短连接池，用完即关）
 */
public final class ExternalDataSourceFactory {

    private ExternalDataSourceFactory() {
    }

    public static String resolveSchema(ExternalDbConnection conn) {
        String dbType = normalizeDbType(conn.getDbType());
        if ("postgresql".equals(dbType) || "postgres".equals(dbType)) {
            return StringUtils.isNotEmpty(conn.getSchema()) ? conn.getSchema().trim() : "public";
        }
        if ("oracle".equals(dbType)) {
            if (StringUtils.isNotEmpty(conn.getSchema())) {
                return conn.getSchema().trim().toUpperCase();
            }
            return conn.getUsername() == null ? "" : conn.getUsername().trim().toUpperCase();
        }
        if ("sqlserver".equals(dbType) || "mssql".equals(dbType)) {
            return StringUtils.isNotEmpty(conn.getSchema()) ? conn.getSchema().trim() : "dbo";
        }
        return conn.getDatabase();
    }

    public static String normalizeDbType(String dbType) {
        return dbType == null ? "mysql" : dbType.trim().toLowerCase();
    }

    public static String normalizeOracleConnectMode(String mode) {
        if (StringUtils.isEmpty(mode)) {
            return "service";
        }
        return mode.trim().toLowerCase();
    }

    public static String buildJdbcUrl(ExternalDbConnection conn) {
        String dbType = normalizeDbType(conn.getDbType());
        if ("mysql".equals(dbType)) {
            return buildMySqlUrl(conn);
        }
        if ("postgresql".equals(dbType) || "postgres".equals(dbType)) {
            return buildPostgreSqlUrl(conn);
        }
        if ("oracle".equals(dbType)) {
            return buildOracleUrl(conn);
        }
        if ("sqlserver".equals(dbType) || "mssql".equals(dbType)) {
            return buildSqlServerUrl(conn);
        }
        throw new BizException("暂不支持的数据库类型：" + dbType);
    }

    private static String buildMySqlUrl(ExternalDbConnection conn) {
        String params = StringUtils.isNotEmpty(conn.getParams())
                ? conn.getParams()
                : "useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai";
        if (!params.startsWith("?")) {
            params = "?" + params;
        } else if (params.length() == 1) {
            params = "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        }
        return String.format("jdbc:mysql://%s:%d/%s%s", conn.getHost(), conn.getPort(), conn.getDatabase(), params);
    }

    private static String buildPostgreSqlUrl(ExternalDbConnection conn) {
        String params = StringUtils.isNotEmpty(conn.getParams()) ? conn.getParams() : "";
        if (StringUtils.isNotEmpty(params) && !params.startsWith("?")) {
            params = "?" + params;
        }
        return String.format("jdbc:postgresql://%s:%d/%s%s", conn.getHost(), conn.getPort(), conn.getDatabase(),
                params);
    }

    private static String buildOracleUrl(ExternalDbConnection conn) {
        String mode = normalizeOracleConnectMode(conn.getOracleConnectMode());
        if ("sid".equals(mode)) {
            return String.format("jdbc:oracle:thin:@%s:%d:%s", conn.getHost(), conn.getPort(), conn.getDatabase());
        }
        if ("tns".equals(mode)) {
            String tns = resolveOracleTns(conn);
            return "jdbc:oracle:thin:@" + tns;
        }
        String params = normalizeOracleParams(conn.getParams());
        return String.format("jdbc:oracle:thin:@//%s:%d/%s%s", conn.getHost(), conn.getPort(), conn.getDatabase(),
                params);
    }

    private static String resolveOracleTns(ExternalDbConnection conn) {
        String tns = StringUtils.isNotEmpty(conn.getParams()) ? conn.getParams().trim() : conn.getDatabase().trim();
        if (StringUtils.isEmpty(tns)) {
            throw new BizException("TNS 模式请填写 DESCRIPTION 连接串");
        }
        if (tns.regionMatches(true, 0, "jdbc:oracle:", 0, "jdbc:oracle:".length())) {
            int at = tns.indexOf('@');
            if (at >= 0 && at < tns.length() - 1) {
                return tns.substring(at + 1);
            }
            throw new BizException("TNS JDBC URL 格式不正确");
        }
        if (tns.startsWith("(")) {
            return tns;
        }
        throw new BizException("TNS 模式请填写以 (DESCRIPTION= 开头的连接串，或完整 jdbc:oracle:thin:@...");
    }

    private static String normalizeOracleParams(String params) {
        if (StringUtils.isEmpty(params)) {
            return "";
        }
        String trimmed = params.trim();
        if (!trimmed.startsWith("?")) {
            trimmed = "?" + trimmed;
        }
        return trimmed;
    }

    private static String buildSqlServerUrl(ExternalDbConnection conn) {
        String params = StringUtils.isNotEmpty(conn.getParams())
                ? conn.getParams()
                : "encrypt=false;trustServerCertificate=true";
        if (params.startsWith("?")) {
            params = params.substring(1);
        }
        if (!params.endsWith(";")) {
            params = params + ";";
        }
        return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;%s", conn.getHost(), conn.getPort(),
                conn.getDatabase(), params);
    }

    public static DataSource create(ExternalDbConnection conn, GenExternalProperties properties) {
        String dbType = normalizeDbType(conn.getDbType());
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(buildJdbcUrl(conn));
        ds.setUsername(conn.getUsername());
        ds.setPassword(conn.getPassword());
        switch (dbType) {
            case "postgresql", "postgres" -> {
                ds.setDriverClassName("org.postgresql.Driver");
                ds.setValidationQuery("SELECT 1");
            }
            case "oracle" -> {
                ds.setDriverClassName("oracle.jdbc.OracleDriver");
                ds.setValidationQuery("SELECT 1 FROM DUAL");
            }
            case "sqlserver", "mssql" -> {
                ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                ds.setValidationQuery("SELECT 1");
            }
            default -> {
                ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                ds.setValidationQuery("SELECT 1");
            }
        }
        ds.setInitialSize(0);
        ds.setMinIdle(0);
        ds.setMaxActive(2);
        ds.setMaxWait(properties.getConnectTimeoutMs());
        ds.setConnectTimeout(properties.getConnectTimeoutMs());
        ds.setSocketTimeout(properties.getQueryTimeoutSeconds() * 1000);
        ds.setTestWhileIdle(false);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(false);
        return ds;
    }

    public static void testConnection(ExternalDbConnection conn, GenExternalProperties properties) {
        assertAllowedHost(conn, properties);
        DataSource ds = create(conn, properties);
        try (Connection connection = ds.getConnection()) {
            if (!connection.isValid(5)) {
                throw new BizException("数据库连接无效");
            }
        } catch (SQLException e) {
            throw new BizException("数据库连接失败：" + e.getMessage());
        } finally {
            closeQuietly(ds);
        }
    }

    public static String queryDbVersion(DataSource ds, ExternalDbConnection conn) {
        String dbType = normalizeDbType(conn.getDbType());
        if ("oracle".equals(dbType)) {
            return querySingleString(ds, "SELECT BANNER FROM v$version WHERE ROWNUM = 1");
        }
        if ("sqlserver".equals(dbType) || "mssql".equals(dbType)) {
            return querySingleString(ds, "SELECT @@VERSION");
        }
        String version = querySingleString(ds, "SELECT version()");
        if (!"unknown".equals(version)) {
            return version;
        }
        return querySingleString(ds, "SELECT VERSION()");
    }

    private static String querySingleString(DataSource ds, String sql) {
        try (Connection connection = ds.getConnection();
             var ps = connection.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ignored) {
            return "unknown";
        }
        return "unknown";
    }

    public static void closeQuietly(DataSource ds) {
        if (ds instanceof DruidDataSource druid) {
            druid.close();
        }
    }

    public static void assertAllowedHost(ExternalDbConnection conn, GenExternalProperties properties) {
        List<String> allowed = properties.getAllowedHosts();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        String host = conn.getHost() == null ? "" : conn.getHost().trim().toLowerCase();
        for (String pattern : allowed) {
            if (StringUtils.isEmpty(pattern)) {
                continue;
            }
            String p = pattern.trim().toLowerCase();
            if (host.equals(p) || host.startsWith(p)) {
                return;
            }
        }
        throw new BizException("连接主机不在允许范围内：" + conn.getHost());
    }
}

