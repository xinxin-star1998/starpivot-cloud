package cn.org.starpivot.system.service.channel;

import cn.org.starpivot.system.domain.entity.SysUserMessage;

import java.util.List;

/**
 * 消息投递渠道接口。
 * <p>
 * 每种外部渠道（站内信、邮件、短信、Webhook）实现该接口，
 * 由 {@link MessageChannelDispatcher} 按 {@link org.springframework.core.annotation.Order} 依次调用。
 * </p>
 */
public interface MessageChannel {

    /**
     * 渠道是否已启用。
     *
     * @return true 表示启用，跳过时返回 false
     */
    boolean isEnabled();

    /**
     * 投递消息。
     *
     * @param messages 已入库的站内消息列表（包含 messageId 与 userId）
     * @param title    消息标题
     * @param content  消息正文
     */
    void send(List<SysUserMessage> messages, String title, String content);
}
