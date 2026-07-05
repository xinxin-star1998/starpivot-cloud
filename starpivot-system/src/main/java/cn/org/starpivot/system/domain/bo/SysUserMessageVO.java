package cn.org.starpivot.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserMessageVO {

    private Long messageId;

    private String msgType;

    private String title;

    private String content;

    private String bizModule;

    private String bizType;

    private String bizKey;

    private Long bizId;

    private String linkPath;

    private String readFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
