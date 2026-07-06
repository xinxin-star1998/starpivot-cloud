package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiHealthVo {

    private boolean online;

    private String message;

    /** 助手头像地址，留空时前端使用默认图 */
    private String botAvatar;

    /** 助手显示名称 */
    private String botName;

    /** 欢迎语（已替换占位符） */
    private String welcomeMessage;

    /** 可选模型列表 */
    private List<AiModelVo> models;

    /** 默认模型 ID */
    private String defaultModel;

    /** 默认采样温度 */
    private Double defaultTemperature;

    /** 每个会话最大记忆条数 */
    private Integer maxMemoryMessages;

    /** 可选 Prompt 场景列表 */
    private List<AiPromptTemplateVo> promptTemplates;

    /** 默认 Prompt 场景 ID */
    private String defaultPromptScene;

    /** 是否启用查询路由（自动场景/模型/RAG） */
    private Boolean queryRouterEnabled;
}
