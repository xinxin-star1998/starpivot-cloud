package cn.org.starpivot.system.domain.dto;

import lombok.Data;

/**
 * 通知公告查询DTO
 * 
 * @author admin
 * @since 2026-02-05
 */
@Data
public class SysNoticeQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
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
