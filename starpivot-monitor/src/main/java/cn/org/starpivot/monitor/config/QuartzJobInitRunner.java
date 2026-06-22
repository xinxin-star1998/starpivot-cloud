package cn.org.starpivot.monitor.config;

import cn.org.starpivot.job.execution.QuartzJobExecution;
import cn.org.starpivot.job.service.ISysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动后初始化 Quartz 定时任务调度。
 * <p>
 * {@link Slf4j}：启用日志；
 * {@link Component}：注册为 Spring 组件；
 * {@link Order}：设置启动顺序为 1，优先于其他 Runner；
 * {@link RequiredArgsConstructor}：注入 {@link ISysJobService}。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class QuartzJobInitRunner implements ApplicationRunner {

    private final ISysJobService sysJobService;

    /**
     * 绑定任务服务并加载数据库中已启用的定时任务。
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        QuartzJobExecution.setSysJobService(sysJobService);
        sysJobService.loadScheduledJobsOnStartup();
        log.info("定时任务调度初始化完成");
    }
}
