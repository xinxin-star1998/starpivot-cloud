package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_task")
public class ApTask {

    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    private Long instanceId;
    private Long stepId;
    private String stepCode;
    private String stepName;
    private Long assigneeId;
    private String status;
    private String action;
    private String comment;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
