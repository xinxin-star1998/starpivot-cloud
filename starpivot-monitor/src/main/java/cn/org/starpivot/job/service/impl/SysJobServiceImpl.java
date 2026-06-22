package cn.org.starpivot.job.service.impl;

import cn.org.starpivot.common.constants.JobConstants;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.job.domain.bo.SysJobLogVO;
import cn.org.starpivot.job.domain.bo.SysJobVO;
import cn.org.starpivot.job.domain.dto.SysJobDTO;
import cn.org.starpivot.job.domain.dto.SysJobLogQueryDTO;
import cn.org.starpivot.job.domain.dto.SysJobQueryDTO;
import cn.org.starpivot.job.domain.entity.SysJob;
import cn.org.starpivot.job.domain.entity.SysJobLog;
import cn.org.starpivot.job.execution.QuartzJobExecution;
import cn.org.starpivot.job.mapper.SysJobLogMapper;
import cn.org.starpivot.job.mapper.SysJobMapper;
import cn.org.starpivot.job.service.ISysJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link ISysJobService} 默认实现，基于 Quartz 调度定时任务并记录执行日志。
 * <p>
 * {@link Slf4j}：启用日志；
 * {@link Service}：注册为 Spring 服务 Bean；
 * {@link RequiredArgsConstructor}：注入任务日志 Mapper、调度器及 Spring 上下文。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

    private final SysJobLogMapper jobLogMapper;
    private final Scheduler scheduler;
    private final ApplicationContext applicationContext;

    private static final String JOB_KEY_PREFIX = "JOB_";

    /**
     * 分页查询定时任务列表。
     *
     * @param query 任务名称、状态等查询条件及分页参数
     * @return {@link SysJobVO} 分页结果
     */
    @Override
    public PageResponse<SysJobVO> selectJobPage(SysJobQueryDTO query) {
        Page<SysJob> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SysJob> result = baseMapper.selectJobPage(page, query);
        List<SysJobVO> list = result.getRecords().stream().map(this::toVO).toList();
        PageResponse<SysJobVO> resp = new PageResponse<>();
        resp.setTotal(result.getTotal());
        resp.setRows(list);
        resp.setPageNum(result.getCurrent());
        resp.setPageSize(result.getSize());
        resp.setPageCount(result.getPages());
        return resp;
    }

    /**
     * 按任务 ID 查询任务详情。
     *
     * @param jobId 任务 ID
     * @return 任务 {@link SysJobVO}
     * @throws BizException 任务不存在时抛出
     */
    @Override
    public SysJobVO getJobById(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) throw new BizException("任务不存在");
        return toVO(job);
    }

    /**
     * 新增定时任务并写入数据库；状态为启用时同步注册到 Quartz 调度器。
     * <p>
     * {@code @Transactional}：保证任务入库与调度注册在同一事务中，任一步骤失败则整体回滚，
     * 避免数据库已写入但调度未生效的不一致状态。
     * </p>
     *
     * @param dto 任务创建参数
     * @return 成功返回 {@code true}
     * @throws BizException 调用目标校验失败或调度注册失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertJob(SysJobDTO dto) {
        validateInvokeTarget(dto.getInvokeTarget());
        SysJob job = toEntity(dto);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        job.setCreateBy(SecurityContextUtils.getUsername());
        job.setUpdateBy(SecurityContextUtils.getUsername());
        save(job);
        if (JobConstants.SUCCESS.equals(job.getStatus())) {
            scheduleJob(job);
        }
        return true;
    }

    /**
     * 更新定时任务：先移除旧调度，再更新数据库，启用状态下重新注册 Quartz 任务。
     * <p>
     * {@code @Transactional}：保证旧调度移除、数据更新与新调度注册原子完成，防止中间态导致重复或遗漏调度。
     * </p>
     *
     * @param dto 任务更新参数，须包含 {@code jobId}
     * @return 成功返回 {@code true}
     * @throws BizException 任务 ID 为空、任务不存在、调用目标校验失败或调度失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(SysJobDTO dto) {
        if (dto.getJobId() == null) throw new BizException("任务ID不能为空");
        validateInvokeTarget(dto.getInvokeTarget());
        SysJob exist = getById(dto.getJobId());
        if (exist == null) throw new BizException("任务不存在");
        unscheduleJob(exist);
        SysJob job = toEntity(dto);
        job.setJobId(dto.getJobId());
        job.setUpdateTime(LocalDateTime.now());
        job.setUpdateBy(SecurityContextUtils.getUsername());
        updateById(job);
        if (JobConstants.SUCCESS.equals(job.getStatus())) {
            scheduleJob(job);
        }
        return true;
    }

    /**
     * 批量删除定时任务，同时从 Quartz 调度器中移除对应 Job。
     * <p>
     * {@code @Transactional}：保证调度移除与数据库删除在同一事务中，避免已删库记录仍被调度执行。
     * </p>
     *
     * @param jobIds 待删除的任务 ID 列表
     * @return 成功返回 {@code true}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJobByIds(List<Long> jobIds) {
        for (Long id : jobIds) {
            SysJob job = getById(id);
            if (job != null) unscheduleJob(job);
        }
        removeByIds(jobIds);
        return true;
    }

    /**
     * 变更任务启停状态：先移除现有调度，更新状态后，启用时重新注册 Quartz 任务。
     * <p>
     * {@code @Transactional}：保证状态变更与调度增删原子完成，防止停用后仍被触发或启用后未注册。
     * </p>
     *
     * @param jobId  任务 ID
     * @param status 目标状态，{@link JobConstants#SUCCESS} 表示启用
     * @return 成功返回 {@code true}
     * @throws BizException 任务不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long jobId, String status) {
        SysJob job = getById(jobId);
        if (job == null) throw new BizException("任务不存在");
        unscheduleJob(job);
        job.setStatus(status);
        job.setUpdateTime(LocalDateTime.now());
        job.setUpdateBy(SecurityContextUtils.getUsername());
        updateById(job);
        if (JobConstants.SUCCESS.equals(status)) scheduleJob(job);
        return true;
    }

    /**
     * 立即触发一次任务执行（不影响 Cron 调度计划）。
     *
     * @param jobId 任务 ID
     * @return 成功返回 {@code true}
     * @throws BizException 任务不存在或 Quartz 触发失败时抛出
     */
    @Override
    public boolean runOnce(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) throw new BizException("任务不存在");
        try {
            JobKey jobKey = JobKey.jobKey(JOB_KEY_PREFIX + jobId);
            if (!scheduler.checkExists(jobKey)) scheduleJob(job);
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            log.error("立即执行任务失败 jobId={}", jobId, e);
            throw new BizException("立即执行失败：" + e.getMessage());
        }
        return true;
    }

    /**
     * 执行指定任务的调用目标并写入执行日志。
     * <p>由 Quartz {@link QuartzJobExecution} 回调，任务不存在时仅记录警告不抛异常。</p>
     *
     * @param jobId 任务 ID
     */
    @Override
    public void executeJob(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) {
            log.warn("定时任务不存在 jobId={}", jobId);
            return;
        }
        SysJobLog logEntity = new SysJobLog();
        logEntity.setJobName(job.getJobName());
        logEntity.setJobGroup(job.getJobGroup());
        logEntity.setInvokeTarget(job.getInvokeTarget());
        logEntity.setCreateTime(LocalDateTime.now());
        try {
            invokeMethod(job.getInvokeTarget());
            logEntity.setStatus(JobConstants.SUCCESS);
            logEntity.setJobMessage("执行成功");
        } catch (Exception e) {
            log.error("定时任务执行异常 jobId={}, target={}", jobId, job.getInvokeTarget(), e);
            logEntity.setStatus(JobConstants.FAIL);
            logEntity.setJobMessage("执行失败");
            logEntity.setExceptionInfo(e.getMessage() != null ? e.getMessage().substring(0, Math.min(2000, e.getMessage().length())) : "");
        }
        jobLogMapper.insert(logEntity);
    }

    /**
     * 分页查询任务执行日志。
     *
     * @param query 任务名称、状态等查询条件及分页参数
     * @return {@link SysJobLogVO} 分页结果
     */
    @Override
    public PageResponse<SysJobLogVO> selectJobLogPage(SysJobLogQueryDTO query) {
        Page<SysJobLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SysJobLog> result = jobLogMapper.selectJobLogPage(page, query);
        List<SysJobLogVO> list = result.getRecords().stream().map(this::logToVO).toList();
        PageResponse<SysJobLogVO> resp = new PageResponse<>();
        resp.setTotal(result.getTotal());
        resp.setRows(list);
        resp.setPageNum(result.getCurrent());
        resp.setPageSize(result.getSize());
        resp.setPageCount(result.getPages());
        return resp;
    }

    /**
     * 清空全部任务执行日志。
     *
     * @return 成功返回 {@code true}
     */
    @Override
    public boolean clearJobLog() {
        jobLogMapper.delete(null);
        return true;
    }

    /**
     * 应用启动时加载所有启用状态的任务到 Quartz 调度器。
     * <p>单个任务加载失败仅记录错误，不影响其余任务注册。</p>
     */
    @Override
    public void loadScheduledJobsOnStartup() {
        List<SysJob> list = lambdaQuery().eq(SysJob::getStatus, JobConstants.SUCCESS).list();
        for (SysJob job : list) {
            try {
                scheduleJob(job);
                log.info("加载定时任务: {} ({})", job.getJobName(), job.getJobId());
            } catch (Exception e) {
                log.error("加载定时任务失败 jobId={}", job.getJobId(), e);
            }
        }
    }

    /**
     * 将任务注册到 Quartz 调度器，按 Cron 表达式创建 Job 与 Trigger。
     * <p>若同 Key 的 Job 已存在则先删除再注册，避免重复调度。</p>
     *
     * @param job 待调度的任务实体
     * @throws BizException Quartz 调度失败时抛出
     */
    private void scheduleJob(SysJob job) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecution.class)
                    .withIdentity(JOB_KEY_PREFIX + job.getJobId())
                    .usingJobData("jobId", job.getJobId())
                    .build();
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("TRIGGER_" + job.getJobId())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()).withMisfireHandlingInstructionDoNothing())
                    .build();
            if (scheduler.checkExists(jobDetail.getKey())) scheduler.deleteJob(jobDetail.getKey());
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("调度任务失败 jobId={}", job.getJobId(), e);
            throw new BizException("调度失败：" + e.getMessage());
        }
    }

    /**
     * 从 Quartz 调度器中移除指定任务的 Job 与 Trigger。
     *
     * @param job 待移除调度的任务实体
     */
    private void unscheduleJob(SysJob job) {
        try {
            JobKey jobKey = JobKey.jobKey(JOB_KEY_PREFIX + job.getJobId());
            if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("移除调度失败 jobId={}", job.getJobId(), e);
        }
    }

    /**
     * 校验任务调用目标字符串的安全性及格式。
     * <p>
     * 检查是否为空、是否包含 {@link JobConstants#JOB_ERROR_STR} 违规字符、
     * 是否以 {@link JobConstants#JOB_WHITELIST_STR} 白名单前缀开头，
     * 并验证格式为 {@code 包名.类名.方法名()}。
     * </p>
     *
     * @param invokeTarget 调用目标，如 {@code cn.org.starpivot.job.task.DemoTask.run()}
     * @throws BizException 校验不通过时抛出
     */
    private void validateInvokeTarget(String invokeTarget) {
        if (invokeTarget == null || invokeTarget.isBlank()) throw new BizException("调用目标不能为空");
        for (String s : JobConstants.JOB_ERROR_STR) {
            if (invokeTarget.contains(s)) throw new BizException("调用目标包含违规字符");
        }
        boolean inWhitelist = false;
        for (String prefix : JobConstants.JOB_WHITELIST_STR) {
            if (invokeTarget.startsWith(prefix)) {
                inWhitelist = true;
                break;
            }
        }
        if (!inWhitelist) throw new BizException("调用目标不在白名单内，仅允许: " + String.join(", ", JobConstants.JOB_WHITELIST_STR));
        if (!invokeTarget.matches("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*\\.([a-zA-Z0-9_]+)\\(\\)$")) {
            throw new BizException("调用目标格式错误，应为: 包名.类名.方法名()");
        }
    }

    /**
     * 通过反射调用 Spring Bean 的无参方法。
     * <p>从 {@code invokeTarget} 解析类名与方法名，经 {@link ApplicationContext} 获取 Bean 后执行。</p>
     *
     * @param invokeTarget 调用目标，格式 {@code 全限定类名.方法名()}
     * @throws Exception 格式错误、类不存在、方法不存在或反射调用失败时抛出
     */
    private void invokeMethod(String invokeTarget) throws Exception {
        int lastDot = invokeTarget.lastIndexOf('.');
        if (lastDot <= 0 || !invokeTarget.endsWith("()")) throw new IllegalArgumentException("调用目标格式错误");
        String className = invokeTarget.substring(0, lastDot);
        String methodPart = invokeTarget.substring(lastDot + 1);
        String methodName = methodPart.substring(0, methodPart.length() - 2);
        Class<?> clazz = Class.forName(className);
        Object bean = applicationContext.getBean(clazz);
        bean.getClass().getMethod(methodName).invoke(bean);
    }

    /**
     * 将 {@link SysJobDTO} 转换为 {@link SysJob} 实体，并为缺失字段填充默认值。
     *
     * @param dto 任务传输对象
     * @return 任务实体
     */
    private SysJob toEntity(SysJobDTO dto) {
        SysJob job = new SysJob();
        job.setJobName(dto.getJobName());
        job.setJobGroup(dto.getJobGroup() != null ? dto.getJobGroup() : "DEFAULT");
        job.setInvokeTarget(dto.getInvokeTarget());
        job.setCronExpression(dto.getCronExpression());
        job.setMisfirePolicy(dto.getMisfirePolicy() != null ? dto.getMisfirePolicy() : "3");
        job.setConcurrent(dto.getConcurrent() != null ? dto.getConcurrent() : "1");
        job.setStatus(dto.getStatus() != null ? dto.getStatus() : "0");
        job.setRemark(dto.getRemark());
        return job;
    }

    /**
     * 将 {@link SysJob} 实体转换为 {@link SysJobVO} 视图对象。
     *
     * @param e 任务实体
     * @return 任务视图对象
     */
    private SysJobVO toVO(SysJob e) {
        SysJobVO vo = new SysJobVO();
        vo.setJobId(e.getJobId());
        vo.setJobName(e.getJobName());
        vo.setJobGroup(e.getJobGroup());
        vo.setInvokeTarget(e.getInvokeTarget());
        vo.setCronExpression(e.getCronExpression());
        vo.setMisfirePolicy(e.getMisfirePolicy());
        vo.setConcurrent(e.getConcurrent());
        vo.setStatus(e.getStatus());
        vo.setCreateBy(e.getCreateBy());
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateBy(e.getUpdateBy());
        vo.setUpdateTime(e.getUpdateTime());
        vo.setRemark(e.getRemark());
        return vo;
    }

    /**
     * 将 {@link SysJobLog} 实体转换为 {@link SysJobLogVO} 视图对象。
     *
     * @param e 任务日志实体
     * @return 任务日志视图对象
     */
    private SysJobLogVO logToVO(SysJobLog e) {
        SysJobLogVO vo = new SysJobLogVO();
        vo.setJobLogId(e.getJobLogId());
        vo.setJobName(e.getJobName());
        vo.setJobGroup(e.getJobGroup());
        vo.setInvokeTarget(e.getInvokeTarget());
        vo.setJobMessage(e.getJobMessage());
        vo.setStatus(e.getStatus());
        vo.setExceptionInfo(e.getExceptionInfo());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}
