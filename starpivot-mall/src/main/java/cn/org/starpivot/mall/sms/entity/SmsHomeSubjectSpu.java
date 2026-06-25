package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页专题 SPU 关联实体。
 * <p>
 * 对应数据库表 {@code sms_home_subject_spu}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_home_subject_spu")
public class SmsHomeSubjectSpu {

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
     * Subject ID
     */
    private Long subjectId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 排序
     */
    private Integer sort;

}
