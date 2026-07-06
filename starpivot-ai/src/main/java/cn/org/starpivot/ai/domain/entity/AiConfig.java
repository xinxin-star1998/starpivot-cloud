package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfig {

    @TableId(type = IdType.AUTO)
    private Long configId;

    private String configName;

    private String botName;

    private String botAvatar;

    private String welcomeMessage;

    private String systemPrompt;

    private String defaultModel;

    private BigDecimal defaultTemperature;

    private Integer maxMemoryMessages;

    private String modelsJson;

    /** 是否启用 RAG：0是 1否 */
    private String ragEnabled;

    private Integer ragTopK;

    /** 是否默认：0是 1否 */
    private String isDefault;

    /** 状态：0正常 1停用 */
    private String status;

    private String remark;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
