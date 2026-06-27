package cn.org.starpivot.approval.service.engine;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.*;
import cn.org.starpivot.approval.mapper.ApInstanceMapper;
import cn.org.starpivot.approval.mapper.ApRecordMapper;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.mq.ApprovalFinishedPublisher;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 审批阶梯流水线引擎。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineEngine {

    private final ApInstanceMapper instanceMapper;
    private final ApTaskMapper taskMapper;
    private final ApRecordMapper recordMapper;
    private final TemplateResolver templateResolver;
    private final AssigneeResolver assigneeResolver;
    private final SpelEvaluator spelEvaluator;
    private final ObjectProvider<ApprovalFinishedPublisher> finishedPublisherProvider;

    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApprovalSubmitRequest request, Long starterId) {
        ensureNoRunningInstance(request.getBizKey());
        Map<String, Object> context = request.getContext() != null ? request.getContext() : Map.of();
        TemplateResolver.ResolvedTemplate resolved = templateResolver.resolve(
                request.getBizModule(), request.getBizType(), request.getTemplateCode(), context);

        ApInstance instance = new ApInstance();
        instance.setTemplateId(resolved.template().getTemplateId());
        instance.setTemplateCode(resolved.template().getTemplateCode());
        instance.setBizModule(request.getBizModule());
        instance.setBizType(request.getBizType());
        instance.setBizKey(request.getBizKey());
        instance.setTitle(request.getTitle());
        instance.setStarterId(starterId);
        instance.setStatus(ApprovalConstants.INSTANCE_RUNNING);
        instance.setContextJson(templateResolver.writeContext(context));
        instance.setCreateTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        saveRecord(instance.getInstanceId(), null, "START", "发起", starterId,
                ApprovalConstants.ACTION_SUBMIT, "发起审批");
        enterStep(instance, resolved, resolved.firstStep());
        return instance.getInstanceId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, String comment, Long operatorId) {
        ApTask task = loadPendingTask(taskId);
        if (!operatorId.equals(task.getAssigneeId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权审批该任务");
        }
        ApInstance instance = loadRunningInstanceForUpdate(task.getInstanceId());
        TemplateResolver.ResolvedTemplate resolved = loadResolved(instance);
        ApTemplateStep currentStep = resolved.findStep(task.getStepId());

        completeTask(task, ApprovalConstants.ACTION_APPROVE, comment);
        saveRecord(instance.getInstanceId(), task.getTaskId(), currentStep.getStepCode(),
                currentStep.getStepName(), operatorId, ApprovalConstants.ACTION_APPROVE, comment);

        if (ApprovalConstants.APPROVE_MODE_ALL.equals(currentStep.getApproveMode())) {
            long pending = taskMapper.selectCount(new LambdaQueryWrapper<ApTask>()
                    .eq(ApTask::getInstanceId, instance.getInstanceId())
                    .eq(ApTask::getStepId, task.getStepId())
                    .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING));
            if (pending > 0) {
                return;
            }
        } else {
            cancelSiblingTasks(instance.getInstanceId(), task.getStepId(), task.getTaskId());
        }
        advanceFrom(instance, resolved, currentStep);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, String comment, Long operatorId) {
        ApTask task = loadPendingTask(taskId);
        if (!operatorId.equals(task.getAssigneeId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权审批该任务");
        }
        ApInstance instance = loadRunningInstanceForUpdate(task.getInstanceId());
        TemplateResolver.ResolvedTemplate resolved = loadResolved(instance);
        ApTemplateStep currentStep = resolved.findStep(task.getStepId());

        completeTask(task, ApprovalConstants.ACTION_REJECT, comment);
        saveRecord(instance.getInstanceId(), task.getTaskId(), currentStep.getStepCode(),
                currentStep.getStepName(), operatorId, ApprovalConstants.ACTION_REJECT, comment);
        cancelAllPending(instance.getInstanceId());
        finishInstance(instance, ApprovalConstants.INSTANCE_REJECTED, comment, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId, Long starterId) {
        ApInstance instance = loadRunningInstanceForUpdate(instanceId);
        if (!starterId.equals(instance.getStarterId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅发起人可撤回");
        }
        doWithdraw(instance, "发起人撤回", true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void systemWithdraw(Long instanceId) {
        ApInstance instance = loadRunningInstanceForUpdate(instanceId);
        doWithdraw(instance, "系统补偿撤回", false);
    }

    private void doWithdraw(ApInstance instance, String recordComment, boolean publishEvent) {
        cancelAllPending(instance.getInstanceId());
        saveRecord(instance.getInstanceId(), null, "WITHDRAW", "撤回", instance.getStarterId(),
                ApprovalConstants.ACTION_WITHDRAW, recordComment);
        finishInstance(instance, ApprovalConstants.INSTANCE_WITHDRAWN, recordComment, publishEvent);
    }

    private void enterStep(ApInstance instance, TemplateResolver.ResolvedTemplate resolved, ApTemplateStep step) {
        Map<String, Object> context = templateResolver.parseContext(instance.getContextJson());
        if (StringUtils.hasText(step.getSkipExpression())
                && spelEvaluator.evaluateBoolean(step.getSkipExpression(), context)) {
            saveRecord(instance.getInstanceId(), null, step.getStepCode(), step.getStepName(),
                    instance.getStarterId(), ApprovalConstants.ACTION_SKIP, "满足跳过条件");
            advanceFrom(instance, resolved, step);
            return;
        }

        List<Long> assigneeIds = assigneeResolver.resolve(step, instance.getStarterId());
        LocalDateTime now = LocalDateTime.now();
        for (Long assigneeId : assigneeIds) {
            ApTask task = new ApTask();
            task.setInstanceId(instance.getInstanceId());
            task.setStepId(step.getStepId());
            task.setStepCode(step.getStepCode());
            task.setStepName(step.getStepName());
            task.setAssigneeId(assigneeId);
            task.setStatus(ApprovalConstants.TASK_PENDING);
            task.setCreateTime(now);
            taskMapper.insert(task);
        }

        ApInstance patch = new ApInstance();
        patch.setInstanceId(instance.getInstanceId());
        patch.setCurrentStepId(step.getStepId());
        instanceMapper.updateById(patch);
        instance.setCurrentStepId(step.getStepId());
    }

    private void advanceFrom(ApInstance instance, TemplateResolver.ResolvedTemplate resolved, ApTemplateStep currentStep) {
        Map<String, Object> context = templateResolver.parseContext(instance.getContextJson());
        ApTemplateStep next = resolveNextStep(resolved, currentStep, context);
        if (next == null) {
            finishInstance(instance, ApprovalConstants.INSTANCE_APPROVED, null, true);
            return;
        }
        enterStep(instance, resolved, next);
    }

    private ApTemplateStep resolveNextStep(TemplateResolver.ResolvedTemplate resolved,
                                           ApTemplateStep currentStep,
                                           Map<String, Object> context) {
        List<ApTemplateRoute> routes = resolved.routes().stream()
                .filter(r -> r.getFromStepId().equals(currentStep.getStepId()))
                .sorted(Comparator.comparing(ApTemplateRoute::getPriority))
                .toList();

        ApTemplateStep defaultNext = null;
        for (ApTemplateRoute route : routes) {
            if (!StringUtils.hasText(route.getConditionExpr())
                    || "default".equalsIgnoreCase(route.getConditionExpr().trim())) {
                defaultNext = resolved.findStep(route.getToStepId());
                continue;
            }
            if (spelEvaluator.evaluateBoolean(route.getConditionExpr(), context)) {
                return resolved.findStep(route.getToStepId());
            }
        }
        if (defaultNext != null) {
            return defaultNext;
        }
        return resolved.nextLinearStep(currentStep);
    }

    private void finishInstance(ApInstance instance, String status, String comment, boolean publishEvent) {
        LocalDateTime now = LocalDateTime.now();
        ApInstance patch = new ApInstance();
        patch.setInstanceId(instance.getInstanceId());
        patch.setStatus(status);
        patch.setFinishTime(now);
        patch.setCurrentStepId(null);
        instanceMapper.updateById(patch);

        ApprovalFinishedPublisher finishedPublisher = finishedPublisherProvider.getIfAvailable();
        if (finishedPublisher != null) {
            finishedPublisher.publish(instance, status, comment, now, publishEvent);
        } else if (publishEvent) {
            log.debug("MQ 未启用，跳过审批完结事件发布: instanceId={}, status={}",
                    instance.getInstanceId(), status);
        }
    }

    private ApInstance loadRunningInstanceForUpdate(Long instanceId) {
        ApInstance instance = instanceMapper.selectByIdForUpdate(instanceId);
        if (instance == null || !ApprovalConstants.INSTANCE_RUNNING.equals(instance.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批实例不存在或已结束");
        }
        return instance;
    }

    private void ensureNoRunningInstance(String bizKey) {
        long running = instanceMapper.selectCount(new LambdaQueryWrapper<ApInstance>()
                .eq(ApInstance::getBizKey, bizKey)
                .eq(ApInstance::getStatus, ApprovalConstants.INSTANCE_RUNNING));
        if (running > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该业务单据已有进行中的审批");
        }
    }

    private ApTask loadPendingTask(Long taskId) {
        ApTask task = taskMapper.selectById(taskId);
        if (task == null || !ApprovalConstants.TASK_PENDING.equals(task.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "待办任务不存在或已处理");
        }
        return task;
    }

    private TemplateResolver.ResolvedTemplate loadResolved(ApInstance instance) {
        return templateResolver.resolve(instance.getBizModule(), instance.getBizType(),
                instance.getTemplateCode(), templateResolver.parseContext(instance.getContextJson()));
    }

    private void completeTask(ApTask task, String action, String comment) {
        ApTask patch = new ApTask();
        patch.setTaskId(task.getTaskId());
        patch.setStatus(ApprovalConstants.TASK_DONE);
        patch.setAction(action);
        patch.setComment(comment);
        patch.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(patch);
    }

    private void cancelSiblingTasks(Long instanceId, Long stepId, Long excludeTaskId) {
        taskMapper.update(null, new LambdaUpdateWrapper<ApTask>()
                .eq(ApTask::getInstanceId, instanceId)
                .eq(ApTask::getStepId, stepId)
                .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING)
                .ne(ApTask::getTaskId, excludeTaskId)
                .set(ApTask::getStatus, ApprovalConstants.TASK_CANCELLED)
                .set(ApTask::getFinishTime, LocalDateTime.now()));
    }

    private void cancelAllPending(Long instanceId) {
        taskMapper.update(null, new LambdaUpdateWrapper<ApTask>()
                .eq(ApTask::getInstanceId, instanceId)
                .eq(ApTask::getStatus, ApprovalConstants.TASK_PENDING)
                .set(ApTask::getStatus, ApprovalConstants.TASK_CANCELLED)
                .set(ApTask::getFinishTime, LocalDateTime.now()));
    }

    private void saveRecord(Long instanceId, Long taskId, String stepCode, String stepName,
                            Long operatorId, String action, String comment) {
        ApRecord record = new ApRecord();
        record.setInstanceId(instanceId);
        record.setTaskId(taskId);
        record.setStepCode(stepCode);
        record.setStepName(stepName);
        record.setOperatorId(operatorId);
        record.setAction(action);
        record.setComment(comment);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
    }
}
