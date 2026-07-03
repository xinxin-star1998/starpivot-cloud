package cn.org.starpivot.mall.mq;


import cn.org.starpivot.api.approval.dto.ApprovalFinishedMessage;
import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApprovalService;
import cn.org.starpivot.mq.config.RabbitListenerConfiguration;
import cn.org.starpivot.mq.core.MessageEnvelope;
import cn.org.starpivot.mq.core.MqMessageConverter;
import cn.org.starpivot.mq.listener.AbstractMqListener;
import cn.org.starpivot.mq.listener.IdempotentChecker;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;



@Slf4j

@Component

@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")

public class MallOrderApprovalMqListener extends AbstractMqListener<ApprovalFinishedMessage> {



    private final OmsOrderReturnApprovalService returnApprovalService;



    public MallOrderApprovalMqListener(MqMessageConverter messageConverter,

                                       ObjectProvider<IdempotentChecker> idempotentCheckerProvider,

                                       OmsOrderReturnApprovalService returnApprovalService) {

        super(messageConverter, idempotentCheckerProvider);

        this.returnApprovalService = returnApprovalService;

    }



    @RabbitListener(

            queues = MqQueueNames.MALL_APPROVAL_FINISHED_RETURN,

            ackMode = "MANUAL",

            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)

    public void onMessage(Message message, Channel channel) throws IOException {

        handleMessage(message, channel, ApprovalFinishedMessage.class, this::dispatch);

    }



    @Override

    protected String resolveIdempotentKey(Message message, MessageEnvelope<ApprovalFinishedMessage> envelope) {

        ApprovalFinishedMessage payload = envelope.getPayload();

        if (payload != null && payload.getInstanceId() != null && payload.getResult() != null) {

            return "approval:finished:order:" + payload.getInstanceId() + ":" + payload.getResult();

        }

        return super.resolveIdempotentKey(message, envelope);

    }



    private void dispatch(ApprovalFinishedMessage msg) {

        returnApprovalService.handleApprovalFinished(

                msg.getBizModule(), msg.getBizType(), msg.getBizKey(), msg.getResult(), msg.getComment());

    }

}

