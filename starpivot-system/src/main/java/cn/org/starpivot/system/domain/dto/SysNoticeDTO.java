package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知公告DTO
 * 
 * @author admin
 * @date 2026-02-05
 */
@Data
public class SysNoticeDTO {

    /**
     * 公告ID
     */
    private Integer noticeId;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题长度不能超过50个字符")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @NotBlank(message = "公告类型（1通知 2公告）不能为空")
    @Size(max = 255, message = "公告类型（1通知 2公告）长度不能超过255个字符")
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;

}
