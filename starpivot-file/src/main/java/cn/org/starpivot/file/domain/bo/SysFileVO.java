package cn.org.starpivot.file.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件列表/详情 VO。
 */
@Data
public class SysFileVO {

    private Long fileId;

    private Long folderId;

    private String category;

    private String categoryLabel;

    private String mediaType;

    private String mediaTypeLabel;

    private String fileName;

    private String fileExt;

    private String contentType;

    private Long fileSize;

    private String objectName;

    private String storageProvider;

    private String bizType;

    private String bizId;

    private String displayUrl;

    /** 预览模式：image / video / audio / pdf / download */
    private String previewMode;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String remark;

    private String folderName;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private String deleteBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deleteTime;
}
