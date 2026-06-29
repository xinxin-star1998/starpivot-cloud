package cn.org.starpivot.file.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件业务引用 sys_file_ref。
 */
@Data
@TableName("sys_file_ref")
public class SysFileRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "ref_id", type = IdType.AUTO)
    private Long refId;

    private Long fileId;

    private String bizType;

    private String bizId;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
