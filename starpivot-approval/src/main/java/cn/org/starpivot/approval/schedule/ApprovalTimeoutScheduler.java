package cn.org.starpivot.approval.schedule;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.ApTask;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.service.engine.PipelineEngine;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫描超时待办并自动处理。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalTimeoutScheduler {

    private final ApTaskMapper taskMapper;
    private final PipelineEngine pipelineEngine;

    @Value("${starpivot.approval.timeout-batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${starpivot.approval.timeout-scan-ms:60000}")
    public void scanOverdueTasks() {
        List<ApTask> overdue = taskMapper.selectList(new LambdaQueryWrapper<ApTask>()
                .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING)
                .isNotNull(ApTask::getDueTime)
                .lt(ApTask::getDueTime, LocalDateTime.now())
                .orderByAsc(ApTask::getDueTime)
                .last("LIMIT " + Math.max(1, batchSize)));
        for (ApTask task : overdue) {
            try {
                pipelineEngine.handleTimeout(task.getTaskId());
            } catch (Exception ex) {
                log.warn("超时任务处理失败: taskId={}, instanceId={}", task.getTaskId(), task.getInstanceId(), ex);
            }
        }
    }
}
