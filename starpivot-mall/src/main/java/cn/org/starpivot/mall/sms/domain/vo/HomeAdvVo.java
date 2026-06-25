package cn.org.starpivot.mall.sms.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页广告视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeAdvVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * pic
     */
    private String pic;
    /**
     * start时间
     */
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * end时间
     */
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * click数量
     */
    private Integer clickCount;
    /**
     * url
     */
    private String url;
    /**
     * note
     */
    private String note;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * Publisher ID
     */
    private Long publisherId;
    /**
     * Auth ID
     */
    private Long authId;
}
