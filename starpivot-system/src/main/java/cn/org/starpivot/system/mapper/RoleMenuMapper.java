package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-菜单关联 Mapper 接口。
 * <p>管理 {@link RoleMenu} 关联表的批量保存与查询。</p>
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /** 批量保存角色与菜单的关联关系。 */
    void batchSave(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    void deleteByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
