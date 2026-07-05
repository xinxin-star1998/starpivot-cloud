package cn.org.starpivot.system.service;

import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.api.system.dto.MessageSendRequest;
import cn.org.starpivot.api.system.dto.MessageSendToRolesRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.SysUserMessageVO;
import cn.org.starpivot.system.domain.dto.MessageBroadcastRequest;
import cn.org.starpivot.system.domain.dto.SysUserMessageQueryDTO;

public interface ISysUserMessageService {

    PageResponse<SysUserMessageVO> pageMyMessages(SysUserMessageQueryDTO query, Long userId);

    long unreadCount(Long userId);

    void markRead(Long messageId, Long userId);

    void markAllRead(Long userId);

    void sendMessage(MessageSendRequest request);

    void sendToRoles(MessageSendToRolesRequest request);

    int broadcast(MessageBroadcastRequest request);
}
