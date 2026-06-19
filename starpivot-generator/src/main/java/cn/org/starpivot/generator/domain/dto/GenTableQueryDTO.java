package cn.org.starpivot.generator.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成表查询参数
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenTableQueryDTO extends PageReqBo {

    /**
     * 表名称（模糊）
     */
    private String tableName;

    /**
     * 表描述（模糊）
     */
    private String tableComment;

    /**
     * 实体类名称（模糊）
     */
    private String className;
}

