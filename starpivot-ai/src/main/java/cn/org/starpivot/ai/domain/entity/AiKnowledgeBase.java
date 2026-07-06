package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_knowledge_base")
public class AiKnowledgeBase {

    @TableId(type = IdType.AUTO)
    private Long kbId;

    private String kbName;

    private String description;

    private Integer topK;

    private Integer chunkSize;

    private Integer chunkOverlap;

    private String status;

    @TableLogic
    private String delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
