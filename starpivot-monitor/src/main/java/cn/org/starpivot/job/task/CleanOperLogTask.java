package cn.org.starpivot.job.task;

import cn.org.starpivot.api.system.SysOperLogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanOperLogTask {

    private final SysOperLogClient sysOperLogClient;

    public void cleanOperLog() {
        log.info("开始执行清空操作日志任务");
        sysOperLogClient.cleanAll();
        log.info("清空操作日志完成");
    }
}
