package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    List<SysRole> selectRoleListByUserId(@Param("userId") Long userId);
    IPage<SysRole> selectPageList(Page<SysRole> page, @Param("param") Object roleQueryDTO);
}
