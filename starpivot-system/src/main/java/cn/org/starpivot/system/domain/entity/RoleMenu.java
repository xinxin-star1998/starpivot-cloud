package cn.org.starpivot.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色与菜单关联实体类，对应数据库表 {@code sys_role_menu}。
 * <p>
 * 维护角色与菜单权限的多对多关联关系。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link TableName} — 映射表名 {@code sys_role_menu}</li>
 *   <li>{@link TableId} — 主键，自增策略</li>
 * </ul>
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}
