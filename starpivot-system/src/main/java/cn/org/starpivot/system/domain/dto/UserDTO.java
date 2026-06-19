package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long userId;
    private Long deptId;

    @NotBlank(message = "用户账号不能为空")
    @Size(min = 2, max = 30, message = "用户账号长度必须在2到30个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户账号只能包含字母、数字和下划线")
    private String userName;

    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Size(max = 11, message = "手机号码长度不能超过11个字符")
    private String phonenumber;

    @Pattern(regexp = "^[012]$", message = "性别只能是0(男)、1(女)或2(未知)")
    private String sex;

    @Size(min = 5, max = 20, message = "密码长度必须在5到20个字符之间")
    private String password;

    @Pattern(regexp = "^[01]$", message = "状态只能是0(正常)或1(停用)")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    private List<Long> roleIds;
    private List<Long> postIds;
}
