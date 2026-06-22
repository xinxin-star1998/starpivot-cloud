package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.common.domain.PageReqBo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库表分页查询 DTO，继承 {@link PageReqBo}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类分页字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalTableQueryDTO extends PageReqBo {

    /** 外部库连接会话 ID */
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    /** 表名过滤条件（可选） */
    private String tableName;

    /** 表注释过滤条件（可选） */
    private String tableComment;
}
