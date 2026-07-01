package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.dto.ApTaskActionDto;
import cn.org.starpivot.approval.domain.dto.ApTaskQueryDto;
import cn.org.starpivot.approval.domain.entity.ApInstance;
import cn.org.starpivot.approval.domain.entity.ApTask;
import cn.org.starpivot.approval.domain.vo.ApTaskVo;
import cn.org.starpivot.approval.mapper.ApInstanceMapper;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.service.ApTaskService;
import cn.org.starpivot.approval.service.engine.PipelineEngine;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApTaskServiceImpl implements ApTaskService {

    private final ApTaskMapper taskMapper;
    private final ApInstanceMapper instanceMapper;
    private final PipelineEngine pipelineEngine;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApTaskVo> todoList(ApTaskQueryDto query, Long assigneeId) {
        return pageTasks(query, assigneeId, ApprovalConstants.TASK_PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApTaskVo> doneList(ApTaskQueryDto query, Long assigneeId) {
        return pageTasks(query, assigneeId, ApprovalConstants.TASK_DONE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ApTaskActionDto dto, Long operatorId) {
        pipelineEngine.approve(dto.getTaskId(), dto.getComment(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ApTaskActionDto dto, Long operatorId) {
        if (!StringUtils.hasText(dto.getComment()) || dto.getComment().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "驳回时必须填写审批意见");
        }
        pipelineEngine.reject(dto.getTaskId(), dto.getComment().trim(), operatorId);
    }

    private PageResponse<ApTaskVo> pageTasks(ApTaskQueryDto query, Long assigneeId, String status) {
        List<Long> instanceFilterIds = resolveInstanceFilterIds(query);
        if (instanceFilterIds != null && instanceFilterIds.isEmpty()) {
            return emptyPage(query);
        }

        Page<ApTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApTask::getAssigneeId, assigneeId)
                .eq(ApTask::getStatus, status)
                .in(instanceFilterIds != null, ApTask::getInstanceId, instanceFilterIds)
                .orderByDesc(ApTask::getTaskId);
        Page<ApTask> taskPage = taskMapper.selectPage(page, wrapper);
        List<ApTask> tasks = taskPage.getRecords();
        Map<Long, ApInstance> instanceMap = loadInstances(tasks);

        List<ApTaskVo> rows = tasks.stream()
                .map(task -> toVo(task, instanceMap.get(task.getInstanceId())))
                .toList();

        PageResponse<ApTaskVo> response = new PageResponse<>();
        response.setTotal(taskPage.getTotal());
        response.setPageNum(taskPage.getCurrent());
        response.setPageSize(taskPage.getSize());
        response.setPageCount(taskPage.getPages());
        response.setRows(rows);
        return response;
    }

    private List<Long> resolveInstanceFilterIds(ApTaskQueryDto query) {
        if (!StringUtils.hasText(query.getTitle())
                && !StringUtils.hasText(query.getBizModule())
                && !StringUtils.hasText(query.getBizType())) {
            return null;
        }
        LambdaQueryWrapper<ApInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTitle()), ApInstance::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getBizModule()), ApInstance::getBizModule, query.getBizModule())
                .eq(StringUtils.hasText(query.getBizType()), ApInstance::getBizType, query.getBizType());
        return instanceMapper.selectList(wrapper).stream()
                .map(ApInstance::getInstanceId)
                .toList();
    }

    private PageResponse<ApTaskVo> emptyPage(ApTaskQueryDto query) {
        PageResponse<ApTaskVo> response = new PageResponse<>();
        response.setTotal(0L);
        response.setPageNum(query.getPageNum().longValue());
        response.setPageSize(query.getPageSize().longValue());
        response.setPageCount(0L);
        response.setRows(List.of());
        return response;
    }

    private Map<Long, ApInstance> loadInstances(List<ApTask> tasks) {
        List<Long> instanceIds = tasks.stream().map(ApTask::getInstanceId).distinct().toList();
        if (instanceIds.isEmpty()) {
            return Map.of();
        }
        return instanceMapper.selectBatchIds(instanceIds).stream()
                .collect(Collectors.toMap(ApInstance::getInstanceId, Function.identity()));
    }

    private ApTaskVo toVo(ApTask task, ApInstance instance) {
        ApTaskVo vo = new ApTaskVo();
        vo.setTaskId(task.getTaskId());
        vo.setInstanceId(task.getInstanceId());
        vo.setStepCode(task.getStepCode());
        vo.setStepName(task.getStepName());
        vo.setStatus(task.getStatus());
        vo.setAction(task.getAction());
        vo.setComment(task.getComment());
        vo.setCreateTime(task.getCreateTime());
        vo.setFinishTime(task.getFinishTime());
        if (instance != null) {
            vo.setTitle(instance.getTitle());
            vo.setBizModule(instance.getBizModule());
            vo.setBizType(instance.getBizType());
            vo.setBizKey(instance.getBizKey());
        }
        return vo;
    }
}
