package cn.org.starpivot.file.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 文件迁移到其他文件夹。
 */
@Data
public class SysFileMoveDTO {

    @NotEmpty(message = "文件ID不能为空")
    private List<Long> ids;

    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetFolderId;
}
