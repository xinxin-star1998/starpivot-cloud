package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 内部接口：按角色投递站内消息。
 */
@Data
public class MessageSendToRolesRequest {

    private List<Long> roleIds;

    private List<String> roleKeys;

    @NotBlank(message = "消息类型不能为空")
    private String msgType;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String bizModule;

    private String bizType;

    private String bizKey;

    private Long bizId;

    private String linkPath;
}
