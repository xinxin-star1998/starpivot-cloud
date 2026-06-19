package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

/**
 * 外部库连接结果
 */
@Data
@Builder
public class ExternalConnectResultVO {

    private String sessionId;

    private String database;

    private String dbVersion;

    private int expireMinutes;
}

