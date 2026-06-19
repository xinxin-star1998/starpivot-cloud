package cn.org.starpivot.job.execution;

import cn.org.starpivot.job.service.ISysJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz 任务执行器：从上下文获取 jobId，委托给 Service 执行并记录日志
 *
 * @author StarPivot
 */
@Slf4j
public class QuartzJobExecution implements Job {

    private static ISysJobService sysJobService;

    public static void setSysJobService(ISysJobService service) {
        QuartzJobExecution.sysJobService = service;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getMergedJobDataMap().getLong("jobId");
        if (sysJobService != null && jobId != null && jobId > 0) {
            sysJobService.executeJob(jobId);
        } else {
            log.warn("定时任务执行器未初始化或 jobId 无效: jobId={}", jobId);
        }
    }
}
