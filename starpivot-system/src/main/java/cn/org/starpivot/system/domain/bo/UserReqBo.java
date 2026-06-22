package cn.org.starpivot.system.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询 BO。
 * <p>
 * 继承 {@link PageReqBo}，封装用户列表的筛选条件。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 生成 equals/hashCode，包含父类字段</li>
 * </ul>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserReqBo extends PageReqBo {

    /**
     * 用户账号（模糊匹配）
     */
    private String userName;

    /**
     * 用户昵称（模糊匹配）
     */
    private String nickName;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 手机号码（模糊匹配）
     */
    private String phonenumber;

    /**
     * 用户邮箱（模糊匹配）
     */
    private String email;

    /**
     * 角色ID（筛选拥有该角色的用户）
     */
    private String roleId;

    /**
     * 部门ID（筛选该部门及下级部门的用户）
     */
    private Long deptId;
}
