package cn.org.starpivot.generator.domain.external;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 外部数据库连接信息
 */
@Data
public class ExternalDbConnection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 数据库类型：mysql / postgresql / oracle / sqlserver */
    @NotBlank(message = "数据库类型不能为空")
    private String dbType = "mysql";

    /** PostgreSQL / Oracle / SQL Server 模式名（MySQL 可忽略） */
    private String schema;

    /** Oracle 连接模式：service / sid / tns（RAC 可用 service + SCAN 主机或 tns） */
    private String oracleConnectMode = "service";

    @NotBlank(message = "主机不能为空")
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 3306;

    @NotBlank(message = "库名不能为空")
    private String database;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 额外 JDBC 参数，如 useSSL=false&serverTimezone=Asia/Shanghai */
    private String params;
}

