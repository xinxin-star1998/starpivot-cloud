package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalDraftRequest extends ExternalSessionRequest {

    @NotBlank(message = "表名不能为空")
    private String tableName;

    private String tableComment;

    private String className;

    private String businessName;

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

    private List<GenTableColumn> columns;
}

