package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.approval.domain.entity.ApTask;
import cn.org.starpivot.approval.domain.vo.ApStatisticsVo;
import cn.org.starpivot.approval.mapper.ApInstanceMapper;
import cn.org.starpivot.approval.mapper.ApStatisticsMapper;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.service.ApStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApStatisticsServiceImpl implements ApStatisticsService {

    private final ApInstanceMapper instanceMapper;
    private final ApTaskMapper taskMapper;
    private final ApStatisticsMapper statisticsMapper;

    @Override
    @Transactional(readOnly = true)
    public ApStatisticsVo dashboard(String bizModule) {
        String module = StringUtils.hasText(bizModule) ? bizModule.trim() : null;

        ApStatisticsVo vo = new ApStatisticsVo();
        vo.setRunningCount(countInstances(ApprovalConstants.INSTANCE_RUNNING, module));
        vo.setApprovedCount(countInstances(ApprovalConstants.INSTANCE_APPROVED, module));
        vo.setRejectedCount(countInstances(ApprovalConstants.INSTANCE_REJECTED, module));
        vo.setWithdrawnCount(countInstances(ApprovalConstants.INSTANCE_WITHDRAWN, module));
        vo.setTotalInstances(vo.getRunningCount() + vo.getApprovedCount()
                + vo.getRejectedCount() + vo.getWithdrawnCount());

        vo.setPendingTaskCount(taskMapper.selectCount(new LambdaQueryWrapper<ApTask>()
                .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING)));
        vo.setOverdueTaskCount(taskMapper.selectCount(new LambdaQueryWrapper<ApTask>()
                .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING)
                .isNotNull(ApTask::getDueTime)
                .lt(ApTask::getDueTime, LocalDateTime.now())));

        vo.setAvgFinishHours(statisticsMapper.avgFinishHours());
        vo.setDailyFinished(statisticsMapper.dailyFinished(LocalDateTime.now().minusDays(30)));
        vo.setBizTypeStats(statisticsMapper.bizTypeStats(module));
        return vo;
    }

    private long countInstances(String status, String bizModule) {
        LambdaQueryWrapper<ApInstance> wrapper = new LambdaQueryWrapper<ApInstance>()
                .eq(ApInstance::getStatus, status);
        if (bizModule != null) {
            wrapper.eq(ApInstance::getBizModule, bizModule);
        }
        return instanceMapper.selectCount(wrapper);
    }
}
