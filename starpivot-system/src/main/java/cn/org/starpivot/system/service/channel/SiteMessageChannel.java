package cn.org.starpivot.system.service.channel;

import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.system.domain.bo.SysUserMessageVO;
import cn.org.starpivot.system.domain.entity.SysUserMessage;
import cn.org.starpivot.system.mapper.SysUserMessageMapper;
import cn.org.starpivot.system.service.MessagePushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 站内信渠道（SSE 实时推送），默认最高优先级。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SiteMessageChannel implements MessageChannel {

    private final MessagePushService messagePushService;
    private final SysUserMessageMapper messageMapper;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void send(List<SysUserMessage> messages, String title, String content) {
        for (SysUserMessage row : messages) {
            try {
                long unread = messageMapper.selectCount(new LambdaQueryWrapper<SysUserMessage>()
                        .eq(SysUserMessage::getUserId, row.getUserId())
                        .eq(SysUserMessage::getReadFlag, MessageConstants.READ_FLAG_UNREAD));
                SysUserMessageVO vo = toVo(row);
                messagePushService.publish(row.getUserId(), vo, unread);
            } catch (Exception ex) {
                log.warn("SSE push failed for userId={}", row.getUserId(), ex);
            }
        }
    }

    private SysUserMessageVO toVo(SysUserMessage row) {
        SysUserMessageVO vo = new SysUserMessageVO();
        vo.setMessageId(row.getMessageId());
        vo.setMsgType(row.getMsgType());
        vo.setTitle(row.getTitle());
        vo.setContent(row.getContent());
        vo.setBizModule(row.getBizModule());
        vo.setBizType(row.getBizType());
        vo.setBizKey(row.getBizKey());
        vo.setBizId(row.getBizId());
        vo.setLinkPath(row.getLinkPath());
        vo.setReadFlag(row.getReadFlag());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
