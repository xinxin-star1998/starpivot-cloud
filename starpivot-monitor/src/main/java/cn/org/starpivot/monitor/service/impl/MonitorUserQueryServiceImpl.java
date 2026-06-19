package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import cn.org.starpivot.monitor.mapper.MonitorUserMapper;
import cn.org.starpivot.monitor.service.MonitorUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorUserQueryServiceImpl implements MonitorUserQueryService {

    private final MonitorUserMapper monitorUserMapper;

    @Override
    public List<MonitorUser> listByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return monitorUserMapper.selectByUserIds(userIds);
    }

    @Override
    public MonitorUser getById(Long userId) {
        return userId == null ? null : monitorUserMapper.selectByUserId(userId);
    }
}
