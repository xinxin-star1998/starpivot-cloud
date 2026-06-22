package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

/**
 * 写盘目标目录条目 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalWriteDirEntryVO {

    /** 目录或文件名称 */
    private String name;

    /** 相对或绝对路径 */
    private String path;
}
