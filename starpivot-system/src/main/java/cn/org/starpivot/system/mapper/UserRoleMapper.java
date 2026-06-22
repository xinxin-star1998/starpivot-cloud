package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-角色关联 Mapper 接口。
 * <p>管理 {@link UserRole} 关联表的批量插入与删除。</p>
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    /** 查询用户关联的角色 ID 列表。 */
    List<Long> getRoleIdsByUserId(Long userId);
    int insertBatchUserRoles(@Param("list") List<UserRole> list);
    boolean deleteByRoleIdAndUserId(@Param("roleId") Long roleId, @Param("userId") Long userId);
}
