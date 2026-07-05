package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 内部接口：发送站内消息（单条内容可投递给多个用户）。
 */
@Data
public class MessageSendRequest {

    @NotEmpty(message = "接收用户不能为空")
    private List<Long> userIds;

    @NotBlank(message = "消息类型不能为空")
    private String msgType;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String bizModule;

    private String bizType;

    private String bizKey;

    private Long bizId;

    /** 前端路由，如 /approval/todo */
    private String linkPath;
}
