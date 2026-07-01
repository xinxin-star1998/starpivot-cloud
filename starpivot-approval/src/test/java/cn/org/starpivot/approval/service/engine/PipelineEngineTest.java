package cn.org.starpivot.approval.service.engine;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.*;
import cn.org.starpivot.approval.mapper.ApInstanceMapper;
import cn.org.starpivot.approval.mapper.ApRecordMapper;
import cn.org.starpivot.approval.mapper.ApTaskMapper;
import cn.org.starpivot.approval.mq.ApprovalFinishedPublisher;
import cn.org.starpivot.approval.service.ApprovalNotificationPublisher;
import cn.org.starpivot.common.exception.BizException;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineEngineTest {

    @Mock
    private ApInstanceMapper instanceMapper;
    @Mock
    private ApTaskMapper taskMapper;
    @Mock
    private ApRecordMapper recordMapper;
    @Mock
    private TemplateResolver templateResolver;
    @Mock
    private AssigneeResolver assigneeResolver;
    @Mock
    private SpelEvaluator spelEvaluator;
    @Mock
    private ObjectProvider<ApprovalFinishedPublisher> finishedPublisherProvider;
    @Mock
    private ApprovalNotificationPublisher notificationPublisher;

    @InjectMocks
    private PipelineEngine pipelineEngine;

    private final AtomicLong instanceSeq = new AtomicLong(100);
    private final AtomicLong taskSeq = new AtomicLong(200);

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ApInstance.class);
        TableInfoHelper.initTableInfo(assistant, ApTask.class);
        TableInfoHelper.initTableInfo(assistant, ApRecord.class);
    }

    @BeforeEach
    void setUp() {
        lenient().when(instanceMapper.insert(any(ApInstance.class))).thenAnswer(inv -> {
            ApInstance instance = inv.getArgument(0);
            if (instance.getInstanceId() == null) {
                instance.setInstanceId(instanceSeq.incrementAndGet());
            }
            return 1;
        });
        lenient().when(taskMapper.insert(any(ApTask.class))).thenAnswer(inv -> {
            ApTask task = inv.getArgument(0);
            if (task.getTaskId() == null) {
                task.setTaskId(taskSeq.incrementAndGet());
            }
            return 1;
        });
        lenient().when(recordMapper.insert(any(ApRecord.class))).thenReturn(1);
        lenient().when(instanceMapper.updateById(any(ApInstance.class))).thenReturn(1);
        lenient().when(taskMapper.updateById(any(ApTask.class))).thenReturn(1);
        lenient().when(taskMapper.update(isNull(), any())).thenReturn(1);
        lenient().when(templateResolver.writeContext(any())).thenReturn("{\"amount\":5000}");
        lenient().when(templateResolver.parseContext(any())).thenReturn(Map.of("amount", 5000));
    }

    @Test
    void submit_shouldCreateInstanceAndNotifyAssignees() {
        when(instanceMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        TemplateResolver.ResolvedTemplate resolved = buildTwoStepTemplate();
        when(templateResolver.resolve(anyString(), anyString(), isNull(), anyMap())).thenReturn(resolved);
        when(assigneeResolver.resolve(any(), eq(1L))).thenReturn(List.of(10L));

        ApprovalSubmitRequest request = buildSubmitRequest();
        Long instanceId = pipelineEngine.submit(request, 1L);

        assertNotNull(instanceId);
        verify(taskMapper).insert(any(ApTask.class));
        verify(notificationPublisher).notifyTaskAssigned(any(ApInstance.class), any(ApTemplateStep.class), eq(List.of(10L)));
    }

    @Test
    void submit_shouldRejectWhenRunningInstanceExists() {
        when(instanceMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        assertThrows(BizException.class, () -> pipelineEngine.submit(buildSubmitRequest(), 1L));
    }

    @Test
    void reject_shouldMarkInstanceRejectedAndNotifyStarter() {
        ApInstance instance = runningInstance(101L);
        ApTask task = pendingTask(201L, 101L, 1L, 10L);
        when(taskMapper.selectById(201L)).thenReturn(task);
        when(instanceMapper.selectByIdForUpdate(101L)).thenReturn(instance);
        when(templateResolver.resolve(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(buildTwoStepTemplate());

        pipelineEngine.reject(201L, "不同意", 10L);

        ArgumentCaptor<ApInstance> patchCaptor = ArgumentCaptor.forClass(ApInstance.class);
        verify(instanceMapper).updateById(patchCaptor.capture());
        assertEquals(ApprovalConstants.INSTANCE_REJECTED, patchCaptor.getValue().getStatus());
        verify(notificationPublisher).notifyInstanceFinished(eq(instance), eq(ApprovalConstants.INSTANCE_REJECTED));
    }

    @Test
    void approve_allMode_shouldNotAdvanceWhilePendingExists() {
        ApInstance instance = runningInstance(102L);
        ApTask task = pendingTask(301L, 102L, 1L, 11L);
        when(taskMapper.selectById(301L)).thenReturn(task);
        when(instanceMapper.selectByIdForUpdate(102L)).thenReturn(instance);
        when(taskMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        when(templateResolver.resolve(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(buildAllModeTemplate());

        pipelineEngine.approve(301L, "同意", 11L);

        verify(instanceMapper, never()).updateById(any(ApInstance.class));
    }

    @Test
    void approve_shouldFollowConditionalRoute() {
        ApInstance instance = runningInstance(103L);
        instance.setContextJson("{\"amount\":20000}");
        ApTask task = pendingTask(401L, 103L, 1L, 10L);
        when(taskMapper.selectById(401L)).thenReturn(task);
        when(instanceMapper.selectByIdForUpdate(103L)).thenReturn(instance);
        when(templateResolver.parseContext(any())).thenReturn(Map.of("amount", 20000));
        when(spelEvaluator.evaluateBoolean(eq("#context['amount'] > 10000"), anyMap())).thenReturn(true);
        when(assigneeResolver.resolve(any(), eq(1L))).thenReturn(List.of(20L));
        when(templateResolver.resolve(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(buildRoutedTemplate());

        pipelineEngine.approve(401L, "同意", 10L);

        verify(taskMapper, atLeastOnce()).insert(any(ApTask.class));
    }

    private ApprovalSubmitRequest buildSubmitRequest() {
        ApprovalSubmitRequest request = new ApprovalSubmitRequest();
        request.setBizModule("mall");
        request.setBizType("purchase");
        request.setBizKey("mall:purchase:1");
        request.setTitle("采购单审批 #1");
        request.setContext(new HashMap<>(Map.of("amount", 5000)));
        return request;
    }

    private ApInstance runningInstance(Long id) {
        ApInstance instance = new ApInstance();
        instance.setInstanceId(id);
        instance.setBizModule("mall");
        instance.setBizType("purchase");
        instance.setBizKey("mall:purchase:1");
        instance.setTemplateCode("mall_purchase_default");
        instance.setTitle("采购单审批 #1");
        instance.setStarterId(1L);
        instance.setStatus(ApprovalConstants.INSTANCE_RUNNING);
        instance.setContextJson("{\"amount\":5000}");
        return instance;
    }

    private ApTask pendingTask(Long taskId, Long instanceId, Long stepId, Long assigneeId) {
        ApTask task = new ApTask();
        task.setTaskId(taskId);
        task.setInstanceId(instanceId);
        task.setStepId(stepId);
        task.setStepCode("dept_leader");
        task.setStepName("部门负责人");
        task.setAssigneeId(assigneeId);
        task.setStatus(ApprovalConstants.TASK_PENDING);
        return task;
    }

    private TemplateResolver.ResolvedTemplate buildTwoStepTemplate() {
        ApTemplate template = new ApTemplate();
        template.setTemplateId(1L);
        template.setTemplateCode("mall_purchase_default");
        ApTemplateStep step1 = step(1L, "dept_leader", "部门负责人", 1, ApprovalConstants.APPROVE_MODE_ANY);
        ApTemplateStep step2 = step(2L, "finance", "财务审批", 2, ApprovalConstants.APPROVE_MODE_ANY);
        return new TemplateResolver.ResolvedTemplate(template, List.of(step1, step2), List.of());
    }

    private TemplateResolver.ResolvedTemplate buildAllModeTemplate() {
        ApTemplate template = new ApTemplate();
        template.setTemplateId(1L);
        ApTemplateStep step1 = step(1L, "finance", "财务会签", 1, ApprovalConstants.APPROVE_MODE_ALL);
        ApTemplateStep step2 = step(2L, "director", "总监", 2, ApprovalConstants.APPROVE_MODE_ANY);
        return new TemplateResolver.ResolvedTemplate(template, List.of(step1, step2), List.of());
    }

    private TemplateResolver.ResolvedTemplate buildRoutedTemplate() {
        ApTemplate template = new ApTemplate();
        template.setTemplateId(1L);
        ApTemplateStep step1 = step(1L, "dept_leader", "部门负责人", 1, ApprovalConstants.APPROVE_MODE_ANY);
        ApTemplateStep step2 = step(2L, "finance", "财务", 2, ApprovalConstants.APPROVE_MODE_ANY);
        ApTemplateStep step3 = step(3L, "director", "总监", 3, ApprovalConstants.APPROVE_MODE_ANY);
        ApTemplateRoute route = new ApTemplateRoute();
        route.setFromStepId(1L);
        route.setToStepId(3L);
        route.setPriority(0);
        route.setConditionExpr("#context['amount'] > 10000");
        return new TemplateResolver.ResolvedTemplate(template, List.of(step1, step2, step3), List.of(route));
    }

    private ApTemplateStep step(Long id, String code, String name, int order, String approveMode) {
        ApTemplateStep step = new ApTemplateStep();
        step.setStepId(id);
        step.setStepCode(code);
        step.setStepName(name);
        step.setStepOrder(order);
        step.setAssigneeType(ApprovalConstants.ASSIGNEE_ROLE);
        step.setAssigneeValue("finance");
        step.setApproveMode(approveMode);
        return step;
    }
}
