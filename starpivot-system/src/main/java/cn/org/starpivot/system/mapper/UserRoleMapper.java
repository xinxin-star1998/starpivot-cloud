package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    List<Long> getRoleIdsByUserId(Long userId);
    int insertBatchUserRoles(@Param("list") List<UserRole> list);
    boolean deleteByRoleIdAndUserId(@Param("roleId") Long roleId, @Param("userId") Long userId);
}
