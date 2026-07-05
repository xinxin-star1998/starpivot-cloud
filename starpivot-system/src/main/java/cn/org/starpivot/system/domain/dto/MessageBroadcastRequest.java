package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MessageBroadcastRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    /** ALL / ROLE / USER，见 MessageConstants.TARGET_* */
    @NotBlank(message = "发送范围不能为空")
    private String targetType;

    private List<Long> roleIds;

    private List<Long> userIds;
}
