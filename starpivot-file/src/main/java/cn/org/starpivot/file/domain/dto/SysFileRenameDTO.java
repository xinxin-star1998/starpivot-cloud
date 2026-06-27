package cn.org.starpivot.file.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文件重命名（仅修改展示名称，不改变存储对象 Key）。
 */
@Data
public class SysFileRenameDTO {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255")
    private String fileName;
}
