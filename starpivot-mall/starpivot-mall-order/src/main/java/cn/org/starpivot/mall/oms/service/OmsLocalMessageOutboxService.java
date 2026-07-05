package cn.org.starpivot.mall.oms.service;

/**
 * 本地消息表（Outbox）服务：与业务同事务写入 mq_message，异步投递 RabbitMQ。
 */
public interface OmsLocalMessageOutboxService {

    /**
     * 在业务事务内登记待发送消息。
     *
     * @param messageId   32 位消息 ID（建议 UUID 去横线）
     * @param exchange    目标交换机
     * @param routingKey  路由键
     * @param classType   消息类型
     * @param content     JSON 载荷
     */
    void enqueue(String messageId, String exchange, String routingKey, String classType, String content);

    /**
     * 扫描并投递待发送消息（status=0 或投递失败 status=2）。
     *
     * @return 本次处理条数
     */
    int flushPending();

    /**
     * 投递单条消息（通常在业务事务提交后调用）。
     */
    void flushMessage(String messageId);
}
