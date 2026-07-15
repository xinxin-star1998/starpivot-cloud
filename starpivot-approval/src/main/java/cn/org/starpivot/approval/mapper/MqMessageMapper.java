package cn.org.starpivot.approval.mapper;

import cn.org.starpivot.approval.domain.entity.MqMessage;
import cn.org.starpivot.approval.mq.MqMessageStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

    /**
     * 原子认领消息：将 NEW 或 SEND_ERROR 置为 SENDING。
     *
     * @return 1 表示认领成功，0 表示已被其他实例认领
     */
    @Update("UPDATE mq_message SET message_status = " + MqMessageStatus.SENDING
            + ", update_time = NOW()"
            + " WHERE message_id = #{messageId}"
            + " AND message_status IN (" + MqMessageStatus.NEW + ", " + MqMessageStatus.SEND_ERROR + ")")
    int claimMessage(@Param("messageId") String messageId);

    /**
     * 将超时停留在 SENDING 的消息重置为 SEND_ERROR，避免崩溃后永久卡住。
     */
    @Update("UPDATE mq_message SET message_status = " + MqMessageStatus.SEND_ERROR
            + ", update_time = NOW()"
            + " WHERE message_status = " + MqMessageStatus.SENDING
            + " AND update_time < NOW() - INTERVAL #{timeoutMinutes} MINUTE")
    int resetStaleSending(@Param("timeoutMinutes") int timeoutMinutes);
}
