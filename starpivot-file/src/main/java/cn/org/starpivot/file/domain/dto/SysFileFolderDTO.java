package cn.org.starpivot.file.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件夹新建/编辑。
 */
@Data
public class SysFileFolderDTO {

    private Long folderId;

    @NotBlank(message = "业务分类不能为空")
    private String category;

    private String folderName;

    private Integer orderNum;

    private String status;

    private String remark;
}
