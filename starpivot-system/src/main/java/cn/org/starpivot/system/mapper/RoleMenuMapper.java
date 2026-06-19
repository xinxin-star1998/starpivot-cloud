package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    void batchSave(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    void deleteByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
