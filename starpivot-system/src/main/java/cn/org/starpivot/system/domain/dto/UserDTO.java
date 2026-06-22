package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户新增/编辑 DTO。
 * <p>
 * 用于用户创建与更新接口的请求体，包含角色与岗位关联信息。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link NotBlank}、{@link Email}、{@link Pattern}、{@link Size} — 字段校验</li>
 * </ul>
 */
@Data
public class UserDTO {

    /**
     * 用户ID（编辑时必填）
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 2, max = 30, message = "用户账号长度必须在2到30个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户账号只能包含字母、数字和下划线")
    private String userName;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Size(max = 11, message = "手机号码长度不能超过11个字符")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Pattern(regexp = "^[012]$", message = "性别只能是0(男)、1(女)或2(未知)")
    private String sex;

    /**
     * 密码（新增时填写）
     */
    @Size(min = 5, max = 20, message = "密码长度必须在5到20个字符之间")
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Pattern(regexp = "^[01]$", message = "状态只能是0(正常)或1(停用)")
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 关联的角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 关联的岗位ID列表
     */
    private List<Long> postIds;
}
