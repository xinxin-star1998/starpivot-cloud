package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_index_task")
public class AiIndexTask {

    @TableId(type = IdType.AUTO)
    private Long taskId;

    private Long docId;

    private String taskType;

    private String status;

    private Integer retryCount;

    private String errorMsg;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createTime;
}
