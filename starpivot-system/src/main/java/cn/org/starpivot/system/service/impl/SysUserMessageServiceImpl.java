package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.api.system.dto.MessageSendRequest;
import cn.org.starpivot.api.system.dto.MessageSendToRolesRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.SysUserMessageVO;
import cn.org.starpivot.system.domain.dto.MessageBroadcastRequest;
import cn.org.starpivot.system.domain.dto.SysUserMessageQueryDTO;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.entity.SysUserMessage;
import cn.org.starpivot.system.domain.entity.UserRole;
import cn.org.starpivot.system.mapper.SysRoleMapper;
import cn.org.starpivot.system.mapper.SysUserMapper;
import cn.org.starpivot.system.mapper.SysUserMessageMapper;
import cn.org.starpivot.system.mapper.UserRoleMapper;
import cn.org.starpivot.system.service.ISysUserMessageService;
import cn.org.starpivot.system.service.MessagePushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysUserMessageServiceImpl implements ISysUserMessageService {

    private final SysUserMessageMapper messageMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final UserRoleMapper userRoleMapper;
    private final MessagePushService messagePushService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SysUserMessageVO> pageMyMessages(SysUserMessageQueryDTO query, Long userId) {
        Page<SysUserMessage> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserMessage::getUserId, userId)
                .eq(StringUtils.hasText(query.getReadFlag()), SysUserMessage::getReadFlag, query.getReadFlag())
                .eq(StringUtils.hasText(query.getMsgType()), SysUserMessage::getMsgType, query.getMsgType())
                .orderByDesc(SysUserMessage::getMessageId);
        Page<SysUserMessage> result = messageMapper.selectPage(page, wrapper);
        PageResponse<SysUserMessageVO> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageNum(result.getCurrent());
        response.setPageSize(result.getSize());
        response.setPageCount(result.getPages());
        response.setRows(result.getRecords().stream().map(this::toVo).toList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<SysUserMessage>()
                .eq(SysUserMessage::getUserId, userId)
                .eq(SysUserMessage::getReadFlag, MessageConstants.READ_FLAG_UNREAD));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long messageId, Long userId) {
        SysUserMessage row = messageMapper.selectById(messageId);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "消息不存在");
        }
        SysUserMessage patch = new SysUserMessage();
        patch.setMessageId(messageId);
        patch.setReadFlag(MessageConstants.READ_FLAG_READ);
        messageMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        messageMapper.update(null, new LambdaUpdateWrapper<SysUserMessage>()
                .eq(SysUserMessage::getUserId, userId)
                .eq(SysUserMessage::getReadFlag, MessageConstants.READ_FLAG_UNREAD)
                .set(SysUserMessage::getReadFlag, MessageConstants.READ_FLAG_READ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(MessageSendRequest request) {
        if (CollectionUtils.isEmpty(request.getUserIds())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Map<Long, SysUserMessage> latestByUser = new LinkedHashMap<>();
        for (Long userId : request.getUserIds()) {
            if (userId == null) {
                continue;
            }
            SysUserMessage row = buildRow(userId, request, now);
            messageMapper.insert(row);
            latestByUser.put(userId, row);
        }
        for (Map.Entry<Long, SysUserMessage> entry : latestByUser.entrySet()) {
            long unread = unreadCount(entry.getKey());
            messagePushService.publish(entry.getKey(), toVo(entry.getValue()), unread);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToRoles(MessageSendToRolesRequest request) {
        List<Long> userIds = resolveUserIdsByRoles(request.getRoleIds(), request.getRoleKeys());
        if (userIds.isEmpty()) {
            return;
        }
        MessageSendRequest sendRequest = new MessageSendRequest();
        sendRequest.setUserIds(userIds);
        sendRequest.setMsgType(request.getMsgType());
        sendRequest.setTitle(request.getTitle());
        sendRequest.setContent(request.getContent());
        sendRequest.setBizModule(request.getBizModule());
        sendRequest.setBizType(request.getBizType());
        sendRequest.setBizKey(request.getBizKey());
        sendRequest.setBizId(request.getBizId());
        sendRequest.setLinkPath(request.getLinkPath());
        sendMessage(sendRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int broadcast(MessageBroadcastRequest request) {
        List<Long> userIds = resolveBroadcastTargets(request);
        if (userIds.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "未找到可发送的用户");
        }
        MessageSendRequest sendRequest = new MessageSendRequest();
        sendRequest.setUserIds(userIds);
        sendRequest.setMsgType(MessageConstants.MSG_TYPE_BROADCAST);
        sendRequest.setTitle(request.getTitle());
        sendRequest.setContent(request.getContent());
        sendRequest.setBizModule(MessageConstants.BIZ_MODULE_SYSTEM);
        sendRequest.setBizType("broadcast");
        sendRequest.setLinkPath("/system/message");
        sendMessage(sendRequest);
        return userIds.size();
    }

    private SysUserMessage buildRow(Long userId, MessageSendRequest request, LocalDateTime now) {
        SysUserMessage row = new SysUserMessage();
        row.setUserId(userId);
        row.setMsgType(request.getMsgType());
        row.setTitle(request.getTitle());
        row.setContent(request.getContent());
        row.setBizModule(request.getBizModule());
        row.setBizType(request.getBizType());
        row.setBizKey(request.getBizKey());
        row.setBizId(request.getBizId());
        row.setLinkPath(request.getLinkPath());
        row.setReadFlag(MessageConstants.READ_FLAG_UNREAD);
        row.setCreateTime(now);
        return row;
    }

    private List<Long> resolveBroadcastTargets(MessageBroadcastRequest request) {
        String targetType = request.getTargetType();
        if (MessageConstants.TARGET_ALL.equals(targetType)) {
            return listActiveUserIds();
        }
        if (MessageConstants.TARGET_ROLE.equals(targetType)) {
            return resolveUserIdsByRoles(request.getRoleIds(), null);
        }
        if (MessageConstants.TARGET_USER.equals(targetType)) {
            if (CollectionUtils.isEmpty(request.getUserIds())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "请指定接收用户");
            }
            return request.getUserIds().stream().filter(id -> id != null).distinct().toList();
        }
        throw new BizException(ErrorCode.PARAM_INVALID, "不支持的发送范围: " + targetType);
    }

    private List<Long> resolveUserIdsByRoles(List<Long> roleIds, List<String> roleKeys) {
        Set<Long> resolvedRoleIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(roleIds)) {
            resolvedRoleIds.addAll(roleIds);
        }
        if (!CollectionUtils.isEmpty(roleKeys)) {
            List<SysRole> roles = sysRoleMapper.selectList(
                    Wrappers.<SysRole>lambdaQuery()
                            .in(SysRole::getRoleKey, roleKeys)
                            .eq(SysRole::getDelFlag, "0")
                            .eq(SysRole::getStatus, "0"));
            roles.stream().map(SysRole::getRoleId).forEach(resolvedRoleIds::add);
        }
        if (resolvedRoleIds.isEmpty()) {
            return List.of();
        }
        List<UserRole> bindings = userRoleMapper.selectList(
                Wrappers.<UserRole>lambdaQuery().in(UserRole::getRoleId, resolvedRoleIds));
        if (bindings.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = new HashSet<>();
        for (UserRole binding : bindings) {
            if (binding.getUserId() != null) {
                userIds.add(binding.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return List.of();
        }
        List<SysUser> activeUsers = sysUserMapper.selectList(
                Wrappers.<SysUser>lambdaQuery()
                        .in(SysUser::getUserId, userIds)
                        .eq(SysUser::getStatus, "0")
                        .eq(SysUser::getDelFlag, "0")
                        .select(SysUser::getUserId));
        return activeUsers.stream().map(SysUser::getUserId).distinct().toList();
    }

    private List<Long> listActiveUserIds() {
        return sysUserMapper.selectList(
                        Wrappers.<SysUser>lambdaQuery()
                                .eq(SysUser::getStatus, "0")
                                .eq(SysUser::getDelFlag, "0")
                                .select(SysUser::getUserId))
                .stream()
                .map(SysUser::getUserId)
                .distinct()
                .toList();
    }

    private SysUserMessageVO toVo(SysUserMessage row) {
        SysUserMessageVO vo = new SysUserMessageVO();
        vo.setMessageId(row.getMessageId());
        vo.setMsgType(row.getMsgType());
        vo.setTitle(row.getTitle());
        vo.setContent(row.getContent());
        vo.setBizModule(row.getBizModule());
        vo.setBizType(row.getBizType());
        vo.setBizKey(row.getBizKey());
        vo.setBizId(row.getBizId());
        vo.setLinkPath(row.getLinkPath());
        vo.setReadFlag(row.getReadFlag());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
