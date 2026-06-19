package cn.org.starpivot.job.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务 VO
 *
 * @author StarPivot
 */
@Data
public class SysJobVO {

    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String invokeTarget;
    private String cronExpression;
    private String misfirePolicy;
    private String concurrent;
    private String status;
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    private String remark;
}
