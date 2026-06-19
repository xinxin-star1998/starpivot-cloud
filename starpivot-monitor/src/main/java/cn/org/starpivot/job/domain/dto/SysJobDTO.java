package cn.org.starpivot.job.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 定时任务 DTO
 *
 * @author StarPivot
 */
@Data
public class SysJobDTO {

    private Long jobId;

    /** 任务名称 */
    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    /** 任务组名 */
    private String jobGroup = "DEFAULT";

    /** 调用目标字符串 */
    @NotBlank(message = "调用目标不能为空")
    private String invokeTarget;

    /** cron执行表达式 */
    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;

    /** 计划执行错误策略（1立即执行 2执行一次 3放弃执行） */
    private String misfirePolicy = "3";

    /** 是否并发执行（0允许 1禁止） */
    private String concurrent = "1";

    /** 状态（0正常 1暂停） */
    private String status = "0";

    /** 备注信息 */
    private String remark;
}
