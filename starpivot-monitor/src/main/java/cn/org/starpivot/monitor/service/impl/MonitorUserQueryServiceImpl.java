package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import cn.org.starpivot.monitor.mapper.MonitorUserMapper;
import cn.org.starpivot.monitor.service.MonitorUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link MonitorUserQueryService} 默认实现，委托 {@link MonitorUserMapper} 查询。
 * <p>
 * {@link Service}：注册为 Spring 服务 Bean；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorUserMapper}。
 */
@Service
@RequiredArgsConstructor
public class MonitorUserQueryServiceImpl implements MonitorUserQueryService {

    private final MonitorUserMapper monitorUserMapper;

    /**
     * 按用户 ID 集合批量查询用户信息。
     * <p>传入 {@code null} 或空集合时直接返回空列表，不访问数据库。</p>
     *
     * @param userIds 用户 ID 集合
     * @return {@link MonitorUser} 列表，无匹配时返回空列表
     */
    @Override
    public List<MonitorUser> listByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return monitorUserMapper.selectByUserIds(userIds);
    }

    /**
     * 按用户 ID 查询单个用户信息。
     *
     * @param userId 用户 ID，为 {@code null} 时返回 {@code null}
     * @return 匹配的 {@link MonitorUser}，不存在或 ID 为空时返回 {@code null}
     */
    @Override
    public MonitorUser getById(Long userId) {
        return userId == null ? null : monitorUserMapper.selectByUserId(userId);
    }
}
