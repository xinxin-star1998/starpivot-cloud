package cn.org.starpivot.common.annotation;

import java.lang.annotation.*;

/**
 * 定时任务分布式锁：多实例部署时同一任务仅一个节点执行。
 * <p>
 * 与 {@code @Scheduled} 配合使用；锁 TTL 到期自动释放，任务结束不主动删锁。
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedScheduled {

    /** 任务唯一标识，完整 Redis 键为 {@code schedule_lock:{key}} */
    String key();

    /** 锁持有时间（秒），应略小于 {@code @Scheduled} 调度间隔 */
    long lockTtlSeconds() default 55;

    /** 未获取锁时是否跳过本次执行 */
    boolean skipIfLocked() default true;
}
