package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

/**
 * 外部库生成会话状态
 */
@Data
@Builder
public class ExternalSessionStatusVO {

    private String sessionId;

    private String database;

    private String dbVersion;

    /** 剩余有效秒数 */
    private long remainingSeconds;

    private int expireMinutes;

    /** 会话覆盖的模板目录 */
    private String templateDir;

    /** 实际生效的模板目录（空表示 classpath 内置） */
    private String effectiveTemplateDir;
}

