package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员收藏 SPU实体。
 * <p>
 * 对应数据库表 {@code ums_member_collect_spu}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member_collect_spu")
public class UmsMemberCollectSpu {

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
     * SPU ID
     */
    private Long spuId;

    /**
     * spu名称
     */
    private String spuName;

    /**
     * 图片
     */
    private String spuImg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
