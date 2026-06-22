package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper 接口。
 * <p>提供角色分页查询及用户-角色关联的自定义 SQL。</p>
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /** 查询用户关联的角色列表。 */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    /** 查询用户角色列表（含详细信息）。 */
    List<SysRole> selectRoleListByUserId(@Param("userId") Long userId);
    /** 分页查询角色列表。 */
    IPage<SysRole> selectPageList(Page<SysRole> page, @Param("param") Object roleQueryDTO);
}
