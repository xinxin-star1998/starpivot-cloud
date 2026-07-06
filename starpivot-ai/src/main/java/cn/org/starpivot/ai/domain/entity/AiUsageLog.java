package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_usage_log")
public class AiUsageLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Long userId;

    private String conversationId;

    private String model;

    private String requestType;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long latencyMs;

    /** 0成功 1失败 */
    private String success;

    private String errorMessage;

    private LocalDateTime createTime;
}
