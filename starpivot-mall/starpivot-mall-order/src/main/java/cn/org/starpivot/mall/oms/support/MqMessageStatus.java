package cn.org.starpivot.mall.oms.support;

/**
 * mq_message 消息状态。
 */
public final class MqMessageStatus {

    /** 新建，待投递 */
    public static final int NEW = 0;

    /** 已发送至交换机 */
    public static final int SENT = 1;

    /** 投递失败 */
    public static final int SEND_ERROR = 2;

    /** 消费者已确认抵达 */
    public static final int ARRIVED = 3;

    private MqMessageStatus() {
    }
}
