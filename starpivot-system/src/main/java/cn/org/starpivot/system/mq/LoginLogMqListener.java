package cn.org.starpivot.system.mq;

import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.mq.config.RabbitListenerConfiguration;
import cn.org.starpivot.mq.listener.AbstractMqListener;
import cn.org.starpivot.mq.listener.IdempotentChecker;
import cn.org.starpivot.mq.core.MqMessageConverter;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import cn.org.starpivot.system.service.SysLogininforService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 消费登录日志 MQ 消息并同步落库。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class LoginLogMqListener extends AbstractMqListener<LoginLogDto> {

    private final SysLogininforService sysLogininforService;

    public LoginLogMqListener(MqMessageConverter messageConverter,
                              ObjectProvider<IdempotentChecker> idempotentCheckerProvider,
                              SysLogininforService sysLogininforService) {
        super(messageConverter, idempotentCheckerProvider);
        this.sysLogininforService = sysLogininforService;
    }

    @RabbitListener(
            queues = MqQueueNames.SYSTEM_LOGIN_LOG,
            ackMode = "MANUAL",
            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)
    public void onMessage(Message message, Channel channel) throws IOException {
        handleMessage(message, channel, LoginLogDto.class, this::persist);
    }

    private void persist(LoginLogDto dto) {
        SysLogininfor entity = new SysLogininfor();
        BeanUtils.copyProperties(dto, entity);
        entity.setLoginTime(LocalDateTime.now());
        sysLogininforService.save(entity);
        log.debug("MQ 登录日志已落库: userName={}", dto.getUserName());
    }
}
