package cn.org.starpivot.system.domain.entity;

import cn.org.starpivot.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体类，对应数据库表 {@code sys_user}。
 * <p>
 * 存储用户账号、部门、联系方式、密码及状态等核心信息。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名 {@code sys_user}</li>
 *   <li>{@link TableId} — 主键 {@code user_id}，自增策略</li>
 *   <li>{@link TableLogic} — 逻辑删除标记（继承自 {@link BaseEntity}）</li>
 *   <li>{@link TableField} — 头像字段自动填充</li>
 * </ul>
 */
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Data
public class SysUser extends BaseEntity {
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型（00系统用户）
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像地址
     */
    @TableField(value = "avatar", fill = FieldFill.INSERT_UPDATE)
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginDate;

    /**
     * 密码最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pwdUpdateDate;

    /**
     * 用户角色列表（用于关联查询优化，非数据库字段）
     */
    @TableField(exist = false)
    private List<SysRole> roles;
}
