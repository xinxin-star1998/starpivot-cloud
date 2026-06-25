package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页专题实体。
 * <p>
 * 对应数据库表 {@code sms_home_subject}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_home_subject")
public class SmsHomeSubject {

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
     * title
     */
    private String title;

    /**
     * sub Title
     */
    private String subTitle;

    /**
     * 状态
     */
    private Integer status;

    /**
     * url
     */
    private String url;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图片
     */
    private String img;

}
