package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 商品评论 VO（pms_spu_comment） */

/**
 * 评论视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CommentVo {

    /**
     * 主键 ID
     */
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
