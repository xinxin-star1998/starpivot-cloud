package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 携带会话 ID 的请求
 */
@Data
public class ExternalSessionRequest {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;
}

