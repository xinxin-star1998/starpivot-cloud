package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {

    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @NotBlank(message = "角色权限字符串不能为空")
    @Size(max = 100, message = "角色权限字符串长度不能超过100个字符")
    private String roleKey;

    private Integer roleSort;

    private String dataScope;

    private Integer menuCheckStrictly;

    private Integer deptCheckStrictly;

    private String status;

    private String remark;

    private List<Long> menuIds;
}
