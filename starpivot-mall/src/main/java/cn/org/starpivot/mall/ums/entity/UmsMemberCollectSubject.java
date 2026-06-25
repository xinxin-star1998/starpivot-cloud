package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员收藏专题实体。
 * <p>
 * 对应数据库表 {@code ums_member_collect_subject}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member_collect_subject")
public class UmsMemberCollectSubject {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * Subject ID
     */
    private Long subjectId;

    /**
     * subject名称
     */
    private String subjectName;

    /**
     * 图片
     */
    private String subjectImg;

    /**
     * subject Url
     */
    private String subjectUrl;

}
