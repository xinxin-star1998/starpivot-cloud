package cn.org.starpivot.job.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时任务示例：调用目标为 cn.org.starpivot.job.task.SampleTask.hello()
 *
 * @author StarPivot
 */
@Slf4j
@Component
public class SampleTask {

    /**
     * 示例方法：可在「定时任务」中配置调用目标为 cn.org.starpivot.job.task.SampleTask.hello()
     */
    public void hello() {
        log.info("定时任务示例执行: hello()");
    }
}
