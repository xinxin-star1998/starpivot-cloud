package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiConfigSaveDto {

    private Long configId;

    @NotBlank(message = "配置名称不能为空")
    @Size(max = 64, message = "配置名称不能超过64字")
    private String configName;

    @NotBlank(message = "助手名称不能为空")
    @Size(max = 64, message = "助手名称不能超过64字")
    private String botName;

    @Size(max = 512, message = "头像地址不能超过512字")
    private String botAvatar;

    @Size(max = 2000, message = "欢迎语不能超过2000字")
    private String welcomeMessage;

    @NotBlank(message = "系统提示词不能为空")
    @Size(max = 8000, message = "系统提示词不能超过8000字")
    private String systemPrompt;

    @NotBlank(message = "默认模型不能为空")
    @Size(max = 64, message = "默认模型不能超过64字")
    private String defaultModel;

    @DecimalMin(value = "0.0", message = "温度不能小于0")
    @DecimalMax(value = "2.0", message = "温度不能大于2")
    private BigDecimal defaultTemperature;

    @Min(value = 1, message = "记忆条数至少为1")
    @Max(value = 200, message = "记忆条数不能超过200")
    private Integer maxMemoryMessages;

    private List<AiModelVo> models = new ArrayList<>();

    /** 是否启用 RAG：0是 1否 */
    private String ragEnabled;

    private Integer ragTopK;

    /** 是否默认：0是 1否 */
    private String isDefault;

    /** 状态：0正常 1停用 */
    private String status;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}
