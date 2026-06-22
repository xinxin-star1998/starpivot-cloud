package cn.org.starpivot.system.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色待分配用户查询 BO。
 * <p>
 * 继承 {@link PageReqBo}，用于分页查询尚未分配至指定角色的用户列表。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 生成 equals/hashCode，包含父类字段</li>
 *   <li>{@link NotBlank} — 角色ID非空校验</li>
 * </ul>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AssignUserReqBo extends PageReqBo {

    /**
     * 用户账号（模糊匹配）
     */
    private String userName;

    /**
     * 手机号码（模糊匹配）
     */
    private String phonenumber;

    /**
     * 角色ID
     */
    @NotBlank(message = "角色ID不能为空")
    private String roleId;
}
