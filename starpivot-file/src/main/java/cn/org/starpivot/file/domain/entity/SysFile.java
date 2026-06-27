package cn.org.starpivot.file.domain.entity;

import cn.org.starpivot.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 文件中心元数据 sys_file。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    private Long folderId;

    private String category;

    private String mediaType;

    private String fileName;

    private String fileExt;

    private String contentType;

    private Long fileSize;

    private String objectName;

    private String fileHash;

    private String storageProvider;

    private String bizType;

    private String bizId;

    private String delFlag;

    private String deleteBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deleteTime;
}
