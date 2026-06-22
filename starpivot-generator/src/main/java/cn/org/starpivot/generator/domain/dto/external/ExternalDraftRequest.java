package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库表结构草稿保存请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalDraftRequest extends ExternalSessionRequest {

    /** 表名 */
    @NotBlank(message = "表名不能为空")
    private String tableName;

    /** 表注释 */
    private String tableComment;

    /** 生成的 Java 类名 */
    private String className;

    /** 业务名称（用于路由、权限标识等） */
    private String businessName;

    /** 功能名称（用于菜单、页面标题等） */
    private String functionName;

    /** 树表：树编码字段（列名） */
    private String treeCode;

    /** 树表：树父编码字段（列名） */
    private String treeParentCode;

    /** 树表：树名称字段（列名） */
    private String treeName;

    /** 菜单 SQL：上级菜单 ID */
    private Long parentMenuId;

    /** 主子表：子表表名 */
    private String subTableName;

    /** 主子表：子表外键列名 */
    private String subTableFkName;

    /** 表级页面路径覆盖（可选） */
    private String vuePagePath;

    /** 表级 API 路径覆盖（可选） */
    private String apiPath;

    /** 列定义列表 */
    private List<GenTableColumn> columns;
}
