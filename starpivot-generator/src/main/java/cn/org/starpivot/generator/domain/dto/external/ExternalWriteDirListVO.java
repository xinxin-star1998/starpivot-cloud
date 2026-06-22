package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 写盘目标目录浏览结果 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalWriteDirListVO {

    /** 当前目录路径 */
    private String current;

    /** 父目录路径 */
    private String parent;

    /** 子目录列表 */
    @Builder.Default
    private List<ExternalWriteDirEntryVO> directories = new ArrayList<>();
}
