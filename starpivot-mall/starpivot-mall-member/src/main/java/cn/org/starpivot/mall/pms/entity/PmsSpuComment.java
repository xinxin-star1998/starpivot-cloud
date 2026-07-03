package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评论实体。
 * <p>
 * 对应数据库表 {@code pms_spu_comment}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_spu_comment")
public class PmsSpuComment {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 会员 ID（C 端评价写入）
     */
    private Long memberId;

    /**
     * spu名称
     */
    private String spuName;

    /**
     * memberNick名称
     */
    private String memberNickName;

    /**
     * star
     */
    private Integer star;

    /**
     * member Ip
     */
    private String memberIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * spu Attributes
     */
    private String spuAttributes;

    /**
     * likes数量
     */
    private Integer likesCount;

    /**
     * reply数量
     */
    private Integer replyCount;

    /**
     * resources
     */
    private String resources;

    /**
     * content
     */
    private String content;

    /**
     * member Icon
     */
    private String memberIcon;

    /**
     * 类型
     */
    private Integer commentType;

}
