package cn.org.starpivot.monitor.service;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;

import java.util.Collection;
import java.util.List;

public interface MonitorUserQueryService {

    List<MonitorUser> listByIds(Collection<Long> userIds);

    MonitorUser getById(Long userId);
}
