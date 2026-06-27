package cn.org.starpivot.approval.mq;


import cn.org.starpivot.api.approval.dto.ApprovalFinishedMessage;
import cn.org.starpivot.api.event.MqRoutingKeys;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.mq.core.MqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;



/**

 * 审批完结事件发布（事务提交后发送，避免幽灵消息）。

 */

@Slf4j

@Component

@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")

@RequiredArgsConstructor

public class ApprovalFinishedPublisher {



    private final MqPublisher mqPublisher;



    public void publish(ApInstance instance, String result, String comment, LocalDateTime finishTime) {

        publish(instance, result, comment, finishTime, true);

    }



    public void publish(ApInstance instance, String result, String comment, LocalDateTime finishTime,

                        boolean publishEvent) {

        if (!publishEvent) {

            return;

        }

        ApprovalFinishedMessage message = buildMessage(instance, result, comment, finishTime);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override

                public void afterCommit() {

                    doPublish(message);

                }

            });

            return;

        }

        doPublish(message);

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



    private void doPublish(ApprovalFinishedMessage message) {

        try {

            mqPublisher.publish(MqRoutingKeys.APPROVAL_INSTANCE_FINISHED,

                    MqRoutingKeys.APPROVAL_INSTANCE_FINISHED, message);

        } catch (Exception ex) {

            log.error("审批完结 MQ 发送失败: instanceId={}, result={}",

                    message.getInstanceId(), message.getResult(), ex);

        }

    }

}

