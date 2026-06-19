package cn.org.starpivot.api.system.dto;

import lombok.Data;

@Data
public class LoginLogDto {

    private String userName;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    /** 0成功 1失败 */
    private String status;

    private String msg;
}
