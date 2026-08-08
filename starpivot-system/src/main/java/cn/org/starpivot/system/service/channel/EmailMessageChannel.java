package cn.org.starpivot.system.service.channel;

import cn.org.starpivot.system.domain.entity.SysUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件投递渠道（SMTP）。
 * <p>
 * 当前为骨架实现，接入 Spring JavaMailSender 前 {@link #isEnabled()} 固定返回 {@code false}，
 * 避免配置误开后仅打日志却被当作已发信。
 * </p>
 */
@Slf4j
@Component
@Order(2)
public class EmailMessageChannel implements MessageChannel {

    @Override
    public boolean isEnabled() {
        // stub 实现不得启用，避免配置打开后误以为邮件已真实投递
        return false;
    }

    @Override
    public void send(List<SysUserMessage> messages, String title, String content) {
        // TODO: 注入 JavaMailSender，遍历 messages 查询用户邮箱后发送邮件
        for (SysUserMessage row : messages) {
            log.info("[EmailChannel] userId={}, title={}, content={} (stub, 待接入 JavaMailSender)",
                    row.getUserId(), title, content);
        }
    }
}
