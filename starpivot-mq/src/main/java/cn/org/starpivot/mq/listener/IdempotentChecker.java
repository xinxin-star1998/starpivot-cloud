package cn.org.starpivot.mq.listener;

/**
 * 消息幂等校验 SPI。
 */
public interface IdempotentChecker {

    /**
     * 尝试占用幂等键。
     *
     * @param messageId 消息 ID
     * @return {@code true} 表示首次消费，{@code false} 表示重复
     */
    boolean tryAcquire(String messageId);
}
