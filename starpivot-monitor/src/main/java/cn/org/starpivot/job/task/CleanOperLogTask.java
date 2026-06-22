package cn.org.starpivot.job.task;

import cn.org.starpivot.api.system.SysOperLogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 清空操作日志的 Quartz 可调用任务 Bean。
 * <p>
 * {@link Slf4j}：启用日志；
 * {@link Component}：注册为 Spring Bean，供定时任务反射调用；
 * {@link RequiredArgsConstructor}：注入 {@link SysOperLogClient}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanOperLogTask {

    private final SysOperLogClient sysOperLogClient;

    /**
     * 通过 Feign 调用系统服务清空全部操作日志。
     */
    public void cleanOperLog() {
        log.info("开始执行清空操作日志任务");
        sysOperLogClient.cleanAll();
        log.info("清空操作日志完成");
    }
}
