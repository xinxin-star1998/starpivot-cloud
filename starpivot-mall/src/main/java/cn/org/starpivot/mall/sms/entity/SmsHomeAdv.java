package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页广告实体。
 * <p>
 * 对应数据库表 {@code sms_home_adv}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_home_adv")
public class SmsHomeAdv {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
