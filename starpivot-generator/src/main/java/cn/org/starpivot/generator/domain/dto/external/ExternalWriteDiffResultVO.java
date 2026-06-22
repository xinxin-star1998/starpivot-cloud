package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部库写盘差异批量预览结果 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalWriteDiffResultVO {

    /** 实际比对的输出根目录 */
    private String outputRoot;

    /** 新增文件数量 */
    @Builder.Default
    private int newCount = 0;

    /** 修改文件数量 */
    @Builder.Default
    private int modifiedCount = 0;

    /** 未变化文件数量 */
    @Builder.Default
    private int unchangedCount = 0;

    /** 各文件差异明细列表 */
    @Builder.Default
    private List<ExternalWriteDiffItemVO> files = new ArrayList<>();
}
