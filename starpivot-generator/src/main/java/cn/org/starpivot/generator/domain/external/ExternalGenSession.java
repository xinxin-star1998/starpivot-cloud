package cn.org.starpivot.generator.domain.external;

import cn.org.starpivot.generator.domain.entity.GenTable;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 外部代码生成会话（内存/Redis，不落 gen_table）
 */
@Data
public class ExternalGenSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;

    private ExternalDbConnection connection;

    private GenPathProfile pathProfile;

    private String author;

    /** 连接时的数据库版本（展示用） */
    private String dbVersion;

    /** 表名 -> 生成草稿 */
    private Map<String, GenTable> tableDrafts = new LinkedHashMap<>();

    /** 会话级 Velocity 模板目录（覆盖 gen.external.template-dir） */
    private String templateDir;

    private Instant expireAt;
}

