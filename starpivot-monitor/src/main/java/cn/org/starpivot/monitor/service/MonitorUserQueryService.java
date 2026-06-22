package cn.org.starpivot.monitor.service;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;

import java.util.Collection;
import java.util.List;

/**
 * 监控模块用户只读查询服务接口。
 */
public interface MonitorUserQueryService {

    /**
     * 按用户 ID 集合批量查询用户。
     *
     * @param userIds 用户 ID 集合，空集合时返回空列表
     * @return 用户列表
     */
    List<MonitorUser> listByIds(Collection<Long> userIds);

    /**
     * 按用户 ID 查询单个用户。
     *
     * @param userId 用户 ID
     * @return 用户信息，ID 为空或不存在时返回 {@code null}
     */
    MonitorUser getById(Long userId);
}
