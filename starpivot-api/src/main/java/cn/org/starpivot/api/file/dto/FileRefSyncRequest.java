package cn.org.starpivot.api.file.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 按 OSS 对象路径同步业务引用（先清空该业务下引用，再绑定列表中的文件）。
 */
@Data
public class FileRefSyncRequest {
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotBlank(message = "业务ID不能为空")
    private String bizId;
    /** 可为空，表示仅解除该业务下全部引用 */
    private List<String> objectNames;

}

