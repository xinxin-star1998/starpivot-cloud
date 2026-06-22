package cn.org.starpivot.system.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色用户分配 DTO。
 * <p>
 * 用于向指定角色批量授权用户。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class UserRoleDTO {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 待分配的用户ID列表
     */
    private List<Long> userIds;
}
