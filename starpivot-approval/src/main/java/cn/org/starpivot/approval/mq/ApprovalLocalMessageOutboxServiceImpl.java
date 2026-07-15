package cn.org.starpivot.approval.mq;

import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.api.event.MqRoutingKeys;
import cn.org.starpivot.approval.domain.entity.MqMessage;
import cn.org.starpivot.approval.mapper.MqMessageMapper;
import cn.org.starpivot.mq.core.MessageEnvelope;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class ApprovalLocalMessageOutboxServiceImpl implements ApprovalLocalMessageOutboxService {

    private static final int BATCH_SIZE = 50;
    private static final int SENDING_TIMEOUT_MINUTES = 5;

    private final MqMessageMapper mqMessageMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enqueue(String messageId, String exchange, String routingKey, String classType, String content) {
        if (!StringUtils.hasText(messageId)) {
            throw new IllegalArgumentException("messageId 不能为空");
        }
        if (!StringUtils.hasText(routingKey)) {
            throw new IllegalArgumentException("routingKey 不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        MqMessage existing = mqMessageMapper.selectById(messageId);
        if (existing != null) {
            return;
        }
        MqMessage message = new MqMessage();
        message.setMessageId(messageId);
        message.setContent(content);
        message.setToExchange(StringUtils.hasText(exchange) ? exchange : MqExchangeNames.TOPIC);
        message.setRoutingKey(routingKey);
        message.setClassType(classType);
        message.setMessageStatus(MqMessageStatus.NEW);
        message.setCreateTime(now);
        message.setUpdateTime(now);
        mqMessageMapper.insert(message);
    }

    @Override
    public int flushPending() {
        mqMessageMapper.resetStaleSending(SENDING_TIMEOUT_MINUTES);

        List<MqMessage> pending = mqMessageMapper.selectList(
                Wrappers.<MqMessage>lambdaQuery()
                        .in(MqMessage::getMessageStatus, MqMessageStatus.NEW, MqMessageStatus.SEND_ERROR)
                        .orderByAsc(MqMessage::getCreateTime)
                        .last("LIMIT " + BATCH_SIZE));
        int processed = 0;
        for (MqMessage message : pending) {
            int claimed = mqMessageMapper.claimMessage(message.getMessageId());
            if (claimed == 0) {
                continue;
            }
            if (deliverClaimedMessage(message.getMessageId())) {
                processed++;
            }
        }
        return processed;
    }

    @Override
    public void flushMessage(String messageId) {
        if (!StringUtils.hasText(messageId)) {
            return;
        }
        int claimed = mqMessageMapper.claimMessage(messageId);
        if (claimed == 0) {
            return;
        }
        deliverClaimedMessage(messageId);
    }

    /**
     * 投递已被当前实例认领的消息（状态已为 SENDING）。
     */
    private boolean deliverClaimedMessage(String messageId) {
        MqMessage message = mqMessageMapper.selectById(messageId);
        if (message == null) {
            return false;
        }
        int newStatus;
        try {
            publish(message);
            newStatus = MqMessageStatus.SENT;
        } catch (Exception ex) {
            log.warn("审批本地消息投递失败 messageId={}: {}", messageId, ex.getMessage());
            newStatus = MqMessageStatus.SEND_ERROR;
        }
        MqMessage patch = new MqMessage();
        patch.setMessageId(messageId);
        patch.setMessageStatus(newStatus);
        patch.setUpdateTime(LocalDateTime.now());
        mqMessageMapper.updateById(patch);
        return newStatus == MqMessageStatus.SENT;
    }

    private void publish(MqMessage message) throws Exception {
        Object payload = StringUtils.hasText(message.getContent())
                ? objectMapper.readValue(message.getContent(), Object.class)
                : null;
        // eventType 保持与历史 MqPublisher 一致，便于排查；路由仍用 outbox.routingKey
        String eventType = MqRoutingKeys.APPROVAL_INSTANCE_FINISHED;
        MessageEnvelope<Object> envelope = MessageEnvelope.builder()
                .messageId(message.getMessageId())
                .eventType(eventType)
                .occurredAt(message.getCreateTime() != null ? message.getCreateTime() : LocalDateTime.now())
                .producer(resolveProducer())
                .payload(payload)
                .build();
        byte[] body = objectMapper.writeValueAsBytes(envelope);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setMessageId(message.getMessageId());
        Message amqpMessage = new Message(body, properties);
        rabbitTemplate.send(message.getToExchange(), message.getRoutingKey(), amqpMessage);
    }

    private String resolveProducer() {
        return environment.getProperty("spring.application.name", "starpivot-approval");
    }
}
