package cn.org.starpivot.approval.service;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.approval.domain.entity.ApNotification;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.approval.mapper.ApNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批站内通知（事务提交后落库，不阻塞主流程）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalNotificationPublisher {

    public static final String TYPE_TASK_ASSIGNED = "TASK_ASSIGNED";
    public static final String TYPE_INSTANCE_FINISHED = "INSTANCE_FINISHED";

    private final ApNotificationMapper notificationMapper;

    public void notifyTaskAssigned(ApInstance instance, ApTemplateStep step, List<Long> assigneeIds) {
        if (instance == null || step == null || assigneeIds == null || assigneeIds.isEmpty()) {
            return;
        }
        String title = "待办审批：" + nullToEmpty(instance.getTitle());
        String content = "步骤「" + step.getStepName() + "」待您处理";
        Long instanceId = instance.getInstanceId();
        runAfterCommit(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Long userId : assigneeIds) {
                if (userId == null) {
                    continue;
                }
                insert(userId, TYPE_TASK_ASSIGNED, title, content, instanceId, null, now);
            }
        });
    }

    public void notifyInstanceFinished(ApInstance instance, String result) {
        if (instance == null || instance.getStarterId() == null) {
            return;
        }
        String resultLabel = switch (result) {
            case ApprovalConstants.INSTANCE_APPROVED -> "已通过";
            case ApprovalConstants.INSTANCE_REJECTED -> "已驳回";
            case ApprovalConstants.INSTANCE_WITHDRAWN -> "已撤回";
            default -> result;
        };
        String title = "审批完结：" + nullToEmpty(instance.getTitle());
        String content = "您的申请" + resultLabel;
        Long instanceId = instance.getInstanceId();
        Long starterId = instance.getStarterId();
        runAfterCommit(() -> insert(starterId, TYPE_INSTANCE_FINISHED, title, content, instanceId, null, LocalDateTime.now()));
    }

    private void insert(Long userId, String type, String title, String content,
                        Long instanceId, Long taskId, LocalDateTime createTime) {
        try {
            ApNotification row = new ApNotification();
            row.setUserId(userId);
            row.setNotifyType(type);
            row.setTitle(title);
            row.setContent(content);
            row.setInstanceId(instanceId);
            row.setTaskId(taskId);
            row.setReadFlag("0");
            row.setCreateTime(createTime);
            notificationMapper.insert(row);
        } catch (Exception ex) {
            log.error("审批通知落库失败: userId={}, type={}, instanceId={}", userId, type, instanceId, ex);
        }
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
