package cn.org.starpivot.generator.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码生成表VO
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Data
public class GenTableVO {

    /**
     * 编号
     */
    private Long tableId;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表描述
     */
    private String tableComment;

    /**
     * 关联子表的表名
     */
    private String subTableName;

    /**
     * 子表关联的外键名
     */
    private String subTableFkName;

    /**
     * 实体类名称
     */
    private String className;

    /**
     * 使用的模板（crud单表操作 tree树表操作）
     */
    private String tplCategory;

    /**
     * 前端模板类型（element-ui模版 element-plus模版）
     */
    private String tplWebType;

    /**
     * 生成包路径
     */
    private String packageName;

    /**
     * 生成模块名
     */
    private String moduleName;

    /**
     * 生成业务名
     */
    private String businessName;

    /**
     * 生成功能名
     */
    private String functionName;

    /**
     * 生成功能作者
     */
    private String functionAuthor;

    /**
     * 生成代码方式（固定为 0：zip 压缩包）
     */
    private String genType;

    /**
     * 生成路径（历史字段，已废弃）
     */
    private String genPath;

    /**
     * 其它生成选项
     */
    private String options;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;
}

