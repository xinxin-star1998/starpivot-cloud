package cn.org.starpivot.auth.service;

import cn.org.starpivot.api.event.MqRoutingKeys;
import cn.org.starpivot.api.system.SysLoginLogClient;
import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.mq.core.MqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 登录日志记录：优先 MQ，失败或未启用时回退 Feign。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogRecordService {

    private final ObjectProvider<MqPublisher> mqPublisherProvider;
    private final SysLoginLogClient sysLoginLogClient;

    public void record(LoginLogDto dto) {
        MqPublisher mqPublisher = mqPublisherProvider.getIfAvailable();
        if (mqPublisher != null) {
            try {
                mqPublisher.publish(
                        MqRoutingKeys.AUDIT_LOGIN_LOG_CREATED,
                        MqRoutingKeys.AUDIT_LOGIN_LOG_CREATED,
                        dto);
                return;
            } catch (Exception ex) {
                log.warn("MQ 发送登录日志失败，回退 Feign: userName={}", dto.getUserName(), ex);
            }
        }
        try {
            sysLoginLogClient.saveLoginLog(dto);
        } catch (Exception ex) {
            log.warn("Feign 保存登录日志失败: userName={}", dto.getUserName(), ex);
        }
    }
}
