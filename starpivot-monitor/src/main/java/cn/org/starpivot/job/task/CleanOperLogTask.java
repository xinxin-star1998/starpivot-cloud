package cn.org.starpivot.job.task;

import cn.org.starpivot.api.system.SysOperLogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 清空操作日志的 Quartz 可调用任务 Bean。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanOperLogTask {

    private final SysOperLogClient sysOperLogClient;

    /**
     * 清空全部操作日志。调用目标：
     * {@code cn.org.starpivot.job.task.CleanOperLogTask.cleanOperLog()}
     */
    public void cleanOperLog() {
        log.info("开始执行清空操作日志任务");
        sysOperLogClient.cleanAll();
        log.info("清空操作日志完成");
    }

    /**
     * 只删除指定天数之前的操作日志。调用目标示例：
     * {@code cn.org.starpivot.job.task.CleanOperLogTask.cleanOperLogBeforeDays(30)}
     */
    public void cleanOperLogBeforeDays(int days) {
        log.info("开始执行清理 {} 天前的操作日志任务", days);
        sysOperLogClient.cleanBeforeDays(days);
        log.info("清理 {} 天前操作日志完成", days);
    }
}
