package cn.org.starpivot.ai.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatSendDto {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字")
    private String message;

    /** 可选，不传则服务端按当前用户生成会话 ID */
    private String conversationId;

    /** 重新生成时回滚上一轮 user/assistant 记忆，避免重复堆积 */
    private Boolean regenerate;

    /** 可选，覆盖默认模型 */
    @Size(max = 64, message = "模型名称不能超过64字")
    private String model;

    /** 可选，覆盖默认采样温度（0~2） */
    @DecimalMin(value = "0.0", message = "temperature 不能小于 0")
    @DecimalMax(value = "2.0", message = "temperature 不能大于 2")
    private Double temperature;

    /** 可选，对话场景（对应 prompt-templates 的 id） */
    @Size(max = 32, message = "场景标识不能超过32字")
    private String promptScene;
}
