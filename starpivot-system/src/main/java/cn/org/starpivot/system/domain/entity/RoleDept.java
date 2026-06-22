package cn.org.starpivot.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色与部门关联实体类，对应数据库表 {@code sys_role_dept}。
 * <p>
 * 用于自定义数据权限范围时，维护角色可访问的部门集合。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link TableName} — 映射表名 {@code sys_role_dept}</li>
 *   <li>{@link TableId} — 主键，自增策略</li>
 * </ul>
 */
@Data
@TableName("sys_role_dept")
public class RoleDept {

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
     * 部门ID
     */
    private Long deptId;
}
