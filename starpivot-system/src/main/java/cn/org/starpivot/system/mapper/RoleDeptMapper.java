package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.RoleDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-部门关联 Mapper 接口。
 * <p>管理角色数据权限范围内的部门关联关系。</p>
 */
@Mapper
public interface RoleDeptMapper extends BaseMapper<RoleDept> {
    /** 批量保存角色与部门的关联关系。 */
    void batchSave(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);
    void deleteByRoleId(@Param("roleId") Long roleId);
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}
