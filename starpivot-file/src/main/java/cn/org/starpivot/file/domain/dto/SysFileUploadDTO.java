package cn.org.starpivot.file.domain.dto;

import lombok.Data;

/**
 * 文件上传业务字段（multipart 另传 file、folderId）。
 */
@Data
public class SysFileUploadDTO {

    private String bizType;

    private String bizId;

    private String remark;
}
