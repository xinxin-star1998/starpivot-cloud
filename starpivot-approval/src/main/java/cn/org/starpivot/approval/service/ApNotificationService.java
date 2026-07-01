package cn.org.starpivot.approval.service;

import cn.org.starpivot.approval.domain.dto.ApNotificationQueryDto;
import cn.org.starpivot.approval.domain.vo.ApNotificationVo;
import cn.org.starpivot.common.entity.PageResponse;

public interface ApNotificationService {

    PageResponse<ApNotificationVo> pageList(ApNotificationQueryDto query, Long userId);

    long unreadCount(Long userId);

    void markRead(Long notifyId, Long userId);

    void markAllRead(Long userId);
}
