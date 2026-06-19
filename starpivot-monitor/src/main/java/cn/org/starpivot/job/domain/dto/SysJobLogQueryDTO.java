package cn.org.starpivot.job.domain.dto;

import lombok.Data;

/**
 * 定时任务日志查询 DTO
 *
 * @author StarPivot
 */
@Data
public class SysJobLogQueryDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private String jobName;
    private String jobGroup;
    /** 执行状态（0正常 1失败） */
    private String status;
}
