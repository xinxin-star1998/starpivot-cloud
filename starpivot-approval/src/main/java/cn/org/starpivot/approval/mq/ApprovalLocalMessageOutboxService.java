package cn.org.starpivot.approval.mq;

/**
 * 审批本地消息表（Outbox）：与业务同事务写入 mq_message，提交后异步投递 RabbitMQ。
 */
public interface ApprovalLocalMessageOutboxService {

    /**
     * 在业务事务内登记待发送消息。
     *
     * @param messageId  32 位消息 ID
     * @param exchange   目标交换机
     * @param routingKey 路由键
     * @param classType  消息类型
     * @param content    JSON 载荷
     */
    void enqueue(String messageId, String exchange, String routingKey, String classType, String content);

    /**
     * 扫描并投递待发送消息（NEW / SEND_ERROR）。
     *
     * @return 本次处理条数
     */
    int flushPending();

    /**
     * 投递单条消息（通常在业务事务提交后调用）。
     */
    void flushMessage(String messageId);
}
