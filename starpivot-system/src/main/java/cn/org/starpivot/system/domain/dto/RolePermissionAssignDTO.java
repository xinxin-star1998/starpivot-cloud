package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionAssignDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private String roleName;

    private String roleKey;

    private String dataScope;

    private List<Long> deptIds;
}
