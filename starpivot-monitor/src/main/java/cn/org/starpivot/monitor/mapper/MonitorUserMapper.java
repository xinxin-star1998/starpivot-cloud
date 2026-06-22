package cn.org.starpivot.monitor.mapper;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 监控模块用户查询 Mapper，只读访问 {@code sys_user}。
 * <p>
 * {@link Mapper}：注册为 MyBatis Mapper 接口。
 */
@Mapper
public interface MonitorUserMapper {

    /**
     * 按用户 ID 集合批量查询未删除用户。
     *
     * @param userIds 用户 ID 集合
     * @return 用户列表
     */
    List<MonitorUser> selectByUserIds(@Param("userIds") Collection<Long> userIds);

    /**
     * 按用户 ID 查询单个未删除用户。
     *
     * @param userId 用户 ID
     * @return 用户信息，不存在时返回 {@code null}
     */
    MonitorUser selectByUserId(@Param("userId") Long userId);
}
