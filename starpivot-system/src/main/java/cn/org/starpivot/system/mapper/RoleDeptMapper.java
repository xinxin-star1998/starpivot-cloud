package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.RoleDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleDeptMapper extends BaseMapper<RoleDept> {
    void batchSave(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);
    void deleteByRoleId(@Param("roleId") Long roleId);
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}
