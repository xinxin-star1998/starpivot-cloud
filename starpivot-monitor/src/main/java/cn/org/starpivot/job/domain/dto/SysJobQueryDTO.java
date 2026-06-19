package cn.org.starpivot.job.domain.dto;

import lombok.Data;

/**
 * 定时任务查询 DTO
 *
 * @author StarPivot
 */
@Data
public class SysJobQueryDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 任务名称 */
    private String jobName;
    /** 任务组名 */
    private String jobGroup;
    /** 状态（0正常 1暂停） */
    private String status;
}
