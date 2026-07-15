package cn.org.starpivot.approval.mq;

import cn.org.starpivot.api.approval.dto.ApprovalFinishedMessage;
import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.api.event.MqRoutingKeys;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 审批完结事件发布：业务事务内写入 Outbox，提交后立即尝试投递；失败由定时任务补偿。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ApprovalFinishedPublisher {

    private final ApprovalLocalMessageOutboxService localMessageOutboxService;
    private final ObjectMapper objectMapper;

    public void publish(ApInstance instance, String result, String comment, LocalDateTime finishTime) {
        publish(instance, result, comment, finishTime, true);
    }

    public void publish(ApInstance instance, String result, String comment, LocalDateTime finishTime,
            boolean publishEvent) {
        if (!publishEvent || instance == null || instance.getInstanceId() == null) {
            return;
        }
        ApprovalFinishedMessage message = buildMessage(instance, result, comment, finishTime);
        String routingKey = resolveRoutingKey(message);
        String content;
        try {
            content = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("审批完结消息序列化失败", ex);
        }
        // 同一实例同一终态幂等登记，避免重试 finish 产生多条 outbox
        String messageId = DigestUtils.md5DigestAsHex(
                (instance.getInstanceId() + ":" + result).getBytes(StandardCharsets.UTF_8));
        localMessageOutboxService.enqueue(
                messageId,
                MqExchangeNames.TOPIC,
                routingKey,
                ApprovalFinishedMessage.class.getSimpleName(),
                content);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        localMessageOutboxService.flushMessage(messageId);
                    } catch (Exception ex) {
                        log.warn("审批完结消息提交后投递失败 messageId={}, instanceId={}: {}",
                                messageId, message.getInstanceId(), ex.getMessage());
                    }
                }
            });
        } else {
            localMessageOutboxService.flushMessage(messageId);
        }
    }

    private ApprovalFinishedMessage buildMessage(ApInstance instance, String result, String comment,
            LocalDateTime finishTime) {
        return ApprovalFinishedMessage.builder()
                .instanceId(instance.getInstanceId())
                .bizModule(instance.getBizModule())
                .bizType(instance.getBizType())
                .bizKey(instance.getBizKey())
                .templateCode(instance.getTemplateCode())
                .result(result)
                .starterId(instance.getStarterId())
                .comment(comment)
                .finishTime(finishTime)
                .build();
    }

    private String resolveRoutingKey(ApprovalFinishedMessage message) {
        if ("mall".equals(message.getBizModule()) && StringUtils.hasText(message.getBizType())) {
            return MqRoutingKeys.mallApprovalFinished(message.getBizType());
        }
        return MqRoutingKeys.APPROVAL_INSTANCE_FINISHED;
    }
}
