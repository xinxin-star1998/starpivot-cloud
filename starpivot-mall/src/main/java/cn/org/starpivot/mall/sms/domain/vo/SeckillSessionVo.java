package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀场次视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SeckillSessionVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * start时间
     */
    private LocalDateTime startTime;
    /**
     * end时间
     */
    private LocalDateTime endTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
