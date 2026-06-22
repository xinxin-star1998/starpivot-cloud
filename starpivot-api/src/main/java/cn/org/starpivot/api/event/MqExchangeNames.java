package cn.org.starpivot.api.event;

/**
 * RabbitMQ Exchange 名称常量。
 */
public final class MqExchangeNames {

    public static final String TOPIC = "starpivot.topic";
    public static final String DIRECT = "starpivot.direct";
    public static final String DLX = "starpivot.dlx";

    private MqExchangeNames() {
    }
}
