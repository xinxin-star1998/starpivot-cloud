package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.approval.domain.dto.ApNotificationQueryDto;
import cn.org.starpivot.approval.domain.entity.ApNotification;
import cn.org.starpivot.approval.domain.vo.ApNotificationVo;
import cn.org.starpivot.approval.mapper.ApNotificationMapper;
import cn.org.starpivot.approval.service.ApNotificationService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ApNotificationServiceImpl implements ApNotificationService {

    private final ApNotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApNotificationVo> pageList(ApNotificationQueryDto query, Long userId) {
        Page<ApNotification> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApNotification::getUserId, userId)
                .eq(StringUtils.hasText(query.getReadFlag()), ApNotification::getReadFlag, query.getReadFlag())
                .orderByDesc(ApNotification::getNotifyId);
        Page<ApNotification> result = notificationMapper.selectPage(page, wrapper);
        PageResponse<ApNotificationVo> response = new PageResponse<>();
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
        return notificationMapper.selectCount(new LambdaQueryWrapper<ApNotification>()
                .eq(ApNotification::getUserId, userId)
                .eq(ApNotification::getReadFlag, "0"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long notifyId, Long userId) {
        ApNotification row = notificationMapper.selectById(notifyId);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "通知不存在");
        }
        ApNotification patch = new ApNotification();
        patch.setNotifyId(notifyId);
        patch.setReadFlag("1");
        notificationMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<ApNotification>()
                .eq(ApNotification::getUserId, userId)
                .eq(ApNotification::getReadFlag, "0")
                .set(ApNotification::getReadFlag, "1"));
    }

    private ApNotificationVo toVo(ApNotification row) {
        ApNotificationVo vo = new ApNotificationVo();
        vo.setNotifyId(row.getNotifyId());
        vo.setNotifyType(row.getNotifyType());
        vo.setTitle(row.getTitle());
        vo.setContent(row.getContent());
        vo.setInstanceId(row.getInstanceId());
        vo.setTaskId(row.getTaskId());
        vo.setReadFlag(row.getReadFlag());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
