package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部库表导入结果 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalImportResultVO {

    /** 新导入的表名列表 */
    @Builder.Default
    private List<String> imported = new ArrayList<>();

    /** 覆盖更新的表名列表 */
    @Builder.Default
    private List<String> updated = new ArrayList<>();

    /** 跳过的表名列表（已存在且未覆盖） */
    @Builder.Default
    private List<String> skipped = new ArrayList<>();
}
