package cn.org.starpivot.generator.domain.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 写盘前单文件 diff 项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalWriteDiffItemVO {

    /** 相对输出根目录的路径（/ 分隔） */
    private String path;

    /** NEW / MODIFIED / UNCHANGED */
    private String status;

    /** 磁盘现有内容（NEW 时为 null） */
    private String oldContent;

    /** 即将写入的内容（UNCHANGED 时可省略，与 old 相同） */
    private String newContent;
}

