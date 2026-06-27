package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.dto.ApInstanceQueryDto;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.approval.domain.entity.ApRecord;
import cn.org.starpivot.approval.domain.entity.ApTask;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.approval.domain.vo.ApInstanceVo;
import cn.org.starpivot.approval.mapper.ApInstanceMapper;
import cn.org.starpivot.approval.mapper.ApRecordMapper;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.service.ApInstanceService;
import cn.org.starpivot.approval.service.engine.AssigneeResolver;
import cn.org.starpivot.approval.service.engine.PipelineEngine;
import cn.org.starpivot.approval.service.engine.TemplateResolver;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApInstanceServiceImpl implements ApInstanceService {

    private final PipelineEngine pipelineEngine;
    private final ApInstanceMapper instanceMapper;
    private final ApTaskMapper taskMapper;
    private final ApRecordMapper recordMapper;
    private final TemplateResolver templateResolver;
    private final AssigneeResolver assigneeResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApprovalSubmitRequest request, Long starterId) {
        return pipelineEngine.submit(request, starterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId, Long starterId) {
        pipelineEngine.withdraw(instanceId, starterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void systemWithdraw(Long instanceId) {
        pipelineEngine.systemWithdraw(instanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApInstanceVo> mineList(ApInstanceQueryDto query, Long starterId) {
        Page<ApInstance> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApInstance::getStarterId, starterId)
                .like(StringUtils.hasText(query.getTitle()), ApInstance::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getStatus()), ApInstance::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getBizModule()), ApInstance::getBizModule, query.getBizModule())
                .eq(StringUtils.hasText(query.getBizType()), ApInstance::getBizType, query.getBizType())
                .orderByDesc(ApInstance::getInstanceId);
        Page<ApInstance> result = instanceMapper.selectPage(page, wrapper);
        PageResponse<ApInstanceVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageNum(result.getCurrent());
        response.setPageSize(result.getSize());
        response.setPageCount(result.getPages());
        response.setRows(result.getRecords().stream().map(this::toVo).toList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalTimelineVo timeline(Long instanceId) {
        ApInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批实例不存在");
        }
        List<ApTemplateStep> steps = templateResolver.loadSteps(instance.getTemplateId());
        List<ApRecord> records = recordMapper.selectList(new LambdaQueryWrapper<ApRecord>()
                .eq(ApRecord::getInstanceId, instanceId)
                .orderByAsc(ApRecord::getCreateTime));
        List<ApTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<ApTask>()
                .eq(ApTask::getInstanceId, instanceId));

        Map<String, List<ApRecord>> recordsByStep = records.stream()
                .filter(r -> !ApprovalConstants.ACTION_SUBMIT.equals(r.getAction())
                        && !ApprovalConstants.ACTION_WITHDRAW.equals(r.getAction()))
                .collect(Collectors.groupingBy(ApRecord::getStepCode));

        ApprovalTimelineVo vo = new ApprovalTimelineVo();
        vo.setInstanceId(instance.getInstanceId());
        vo.setTitle(instance.getTitle());
        vo.setStatus(instance.getStatus());
        vo.setBizModule(instance.getBizModule());
        vo.setBizType(instance.getBizType());
        vo.setBizKey(instance.getBizKey());

        List<ApprovalTimelineVo.TimelineStepVo> stepVos = new ArrayList<>();
        for (ApTemplateStep step : steps) {
            ApprovalTimelineVo.TimelineStepVo stepVo = new ApprovalTimelineVo.TimelineStepVo();
            stepVo.setStepCode(step.getStepCode());
            stepVo.setStepName(step.getStepName());
            stepVo.setStatus(resolveStepStatus(step, instance, tasks, records));
            stepVo.setAssignees(resolveAssigneeNames(step, instance, tasks));
            stepVo.setRecords(buildRecords(recordsByStep.getOrDefault(step.getStepCode(), List.of())));
            stepVos.add(stepVo);
        }
        vo.setSteps(stepVos);
        return vo;
    }

    private String resolveStepStatus(ApTemplateStep step, ApInstance instance, List<ApTask> tasks, List<ApRecord> records) {
        boolean skipped = records.stream()
                .anyMatch(r -> step.getStepCode().equals(r.getStepCode())
                        && ApprovalConstants.ACTION_SKIP.equals(r.getAction()));
        if (skipped) {
            return "SKIPPED";
        }
        List<ApTask> stepTasks = tasks.stream().filter(t -> step.getStepId().equals(t.getStepId())).toList();
        if (stepTasks.stream().anyMatch(t -> ApprovalConstants.TASK_PENDING.equals(t.getStatus()))) {
            return "PENDING";
        }
        if (stepTasks.stream().anyMatch(t -> ApprovalConstants.TASK_DONE.equals(t.getStatus()))) {
            return "DONE";
        }
        if (instance.getCurrentStepId() != null && step.getStepOrder() > findStepOrder(instance.getCurrentStepId(), step.getTemplateId())) {
            return "CANCELLED";
        }
        if (ApprovalConstants.INSTANCE_RUNNING.equals(instance.getStatus())
                && instance.getCurrentStepId() != null
                && step.getStepId().equals(instance.getCurrentStepId())) {
            return "PENDING";
        }
        return stepTasks.isEmpty() ? "CANCELLED" : "DONE";
    }

    private int findStepOrder(Long stepId, Long templateId) {
        return templateResolver.loadSteps(templateId).stream()
                .filter(s -> s.getStepId().equals(stepId))
                .map(ApTemplateStep::getStepOrder)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

    private List<String> resolveAssigneeNames(ApTemplateStep step, ApInstance instance, List<ApTask> tasks) {
        return tasks.stream()
                .filter(t -> step.getStepId().equals(t.getStepId()))
                .filter(t -> ApprovalConstants.TASK_PENDING.equals(t.getStatus()))
                .map(t -> assigneeResolver.displayName(t.getAssigneeId()))
                .distinct()
                .toList();
    }

    private List<ApprovalTimelineVo.TimelineRecordVo> buildRecords(List<ApRecord> records) {
        return records.stream()
                .sorted(Comparator.comparing(ApRecord::getCreateTime))
                .map(r -> {
                    ApprovalTimelineVo.TimelineRecordVo item = new ApprovalTimelineVo.TimelineRecordVo();
                    item.setOperatorName(assigneeResolver.displayName(r.getOperatorId()));
                    item.setAction(r.getAction());
                    item.setComment(r.getComment());
                    item.setTime(r.getCreateTime());
                    return item;
                })
                .toList();
    }

    private ApInstanceVo toVo(ApInstance instance) {
        ApInstanceVo vo = new ApInstanceVo();
        BeanUtils.copyProperties(instance, vo);
        vo.setStarterName(assigneeResolver.displayName(instance.getStarterId()));
        return vo;
    }
}
