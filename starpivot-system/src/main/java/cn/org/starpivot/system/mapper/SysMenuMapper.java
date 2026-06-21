package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    List<String> selectAllPermissionStrings();

    List<Long> selectMenuIds(@Param("roleId") Long roleId);

    List<SysMenu> getMenuByRoleId(@Param("roleId") Long roleId);
}