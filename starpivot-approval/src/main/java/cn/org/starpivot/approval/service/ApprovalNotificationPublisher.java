package cn.org.starpivot.approval.service;

import cn.org.starpivot.api.system.SysMessageClient;
import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.api.system.dto.MessageSendRequest;
import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;

/**
 * 审批站内通知：投递到全平台统一消息中心（starpivot-system）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalNotificationPublisher {

    public static final String TYPE_TASK_ASSIGNED = MessageConstants.MSG_TYPE_APPROVAL_TASK;
    public static final String TYPE_INSTANCE_FINISHED = MessageConstants.MSG_TYPE_APPROVAL_RESULT;

    private final SysMessageClient sysMessageClient;

    public void notifyTaskAssigned(ApInstance instance, ApTemplateStep step, List<Long> assigneeIds) {
        if (instance == null || step == null || assigneeIds == null || assigneeIds.isEmpty()) {
            return;
        }
        String title = "待办审批：" + nullToEmpty(instance.getTitle());
        String content = "步骤「" + step.getStepName() + "」待您处理";
        MessageSendRequest request = buildRequest(
                assigneeIds,
                MessageConstants.MSG_TYPE_APPROVAL_TASK,
                title,
                content,
                instance,
                "/approval/todo");
        runAfterCommit(() -> sendQuietly(request));
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
        MessageSendRequest request = buildRequest(
                Collections.singletonList(instance.getStarterId()),
                MessageConstants.MSG_TYPE_APPROVAL_RESULT,
                title,
                content,
                instance,
                "/approval/mine");
        runAfterCommit(() -> sendQuietly(request));
    }

    private MessageSendRequest buildRequest(List<Long> userIds, String msgType, String title, String content,
                                            ApInstance instance, String linkPath) {
        MessageSendRequest request = new MessageSendRequest();
        request.setUserIds(userIds);
        request.setMsgType(msgType);
        request.setTitle(title);
        request.setContent(content);
        request.setBizModule(instance.getBizModule());
        request.setBizType(instance.getBizType());
        request.setBizKey(instance.getBizKey());
        request.setBizId(instance.getInstanceId());
        request.setLinkPath(linkPath);
        return request;
    }

    private void sendQuietly(MessageSendRequest request) {
        try {
            sysMessageClient.send(request);
        } catch (Exception ex) {
            log.error("审批通知投递失败: type={}, title={}", request.getMsgType(), request.getTitle(), ex);
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
