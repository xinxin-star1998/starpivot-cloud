package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> selectPageList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    List<Map<String, Object>> countByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<SysRole> getRolesByUserId(@Param("userId") Long userId);

    SysUser selectUserWithRoles(@Param("userId") Long userId);

    List<SysMenu> getMenuByUserId(@Param("userId") Long userId);

    IPage<SysUser> getUserListByRoleId(Page<SysUser> page, @Param("param") Map<String, Object> param);

    IPage<SysUser> unallocatedList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    Long selectDeptIdByUserId(@Param("userId") Long userId);

    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    List<SysMenu> selectAllActiveMenus();
}
