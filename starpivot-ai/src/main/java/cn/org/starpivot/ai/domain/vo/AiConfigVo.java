package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiConfigVo {

    private Long configId;

    private String configName;

    private String botName;

    private String botAvatar;

    private String welcomeMessage;

    private String systemPrompt;

    private String defaultModel;

    private BigDecimal defaultTemperature;

    private Integer maxMemoryMessages;

    private List<AiModelVo> models = new ArrayList<>();

    private String ragEnabled;

    private Integer ragTopK;

    private String isDefault;

    private String status;

    private String remark;

    private String updateBy;

    private LocalDateTime updateTime;
}
