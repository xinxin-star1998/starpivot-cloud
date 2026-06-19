package cn.org.starpivot.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogininforVO {

    private Long infoId;

    private String userName;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    private String status;

    private String msg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;
}
