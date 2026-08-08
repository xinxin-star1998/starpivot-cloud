package cn.org.starpivot.system.service.channel;

import cn.org.starpivot.system.domain.entity.SysUserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息渠道分发器，按 {@link org.springframework.core.annotation.Order} 依次调用所有已启用的渠道。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageChannelDispatcher {

    private final List<MessageChannel> channels;

    /**
     * 将消息分发到所有已启用的渠道。
     *
     * @param messages 已入库的站内消息列表
     * @param title    消息标题
     * @param content  消息正文
     */
    public void dispatch(List<SysUserMessage> messages, String title, String content) {
        for (MessageChannel channel : channels) {
            if (!channel.isEnabled()) {
                continue;
            }
            try {
                channel.send(messages, title, content);
            } catch (Exception ex) {
                log.warn("MessageChannel [{}] dispatch failed", channel.getClass().getSimpleName(), ex);
            }
        }
    }
}
