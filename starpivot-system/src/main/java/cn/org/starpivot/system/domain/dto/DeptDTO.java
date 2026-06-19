package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeptDTO {

    private Long deptId;

    private Long parentId;

    private String ancestors;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过30个字符")
    private String deptName;

    private Integer orderNum;

    @Size(max = 20, message = "负责人长度不能超过20个字符")
    private String leader;

    @Size(max = 11, message = "联系电话长度不能超过11个字符")
    private String phone;

    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    private String status;

    private String remark;
}
