package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色数据权限分配 DTO。
 * <p>
 * 用于设置角色的数据权限范围及关联部门。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link NotNull} — 角色ID非空校验</li>
 * </ul>
 */
@Data
public class RolePermissionAssignDTO {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符串
     */
    private String roleKey;

    /**
     * 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private String dataScope;

    /**
     * 自定义数据权限关联的部门ID列表
     */
    private List<Long> deptIds;
}
