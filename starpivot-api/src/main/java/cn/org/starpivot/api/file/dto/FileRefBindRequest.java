package cn.org.starpivot.api.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件业务引用绑定请求。
 */
@Data
public class FileRefBindRequest {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "业务ID不能为空")
    private String bizId;
}
