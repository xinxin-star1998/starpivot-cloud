package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 设备会话信息（用户中心「在线设备」展示）。
 */
@Data
@Builder
@Schema(description = "设备会话")
public class DeviceSessionVo {

    @Schema(description = "设备会话 ID")
    private String deviceSessionId;

    @Schema(description = "IP 地址")
    private String ipaddr;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录时间")
    private String createdAt;

    @Schema(description = "最后访问时间")
    private String lastAccessTime;

    @Schema(description = "会话持续时间描述")
    private String sessionDuration;

    @Schema(description = "是否为当前会话")
    private Boolean isCurrent;
}
