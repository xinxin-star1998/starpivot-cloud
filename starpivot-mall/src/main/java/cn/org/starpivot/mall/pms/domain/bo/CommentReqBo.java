package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品评论分页查询（pms_spu_comment） */

/**
 * 评论查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentReqBo extends PageReqBo {

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SKU ID
     */
    private Long skuId;

    /** 会员昵称模糊 */
    /**
     * memberNick名称
     */
    private String memberNickName;

    /** 0-隐藏 1-显示 */
    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * star
     */
    private Integer star;

    /** 会员 ID（C 端「我的评价」） */
    private Long memberId;

    /** 评论类型：0-商品评价 1-回复 */
    private Integer commentType;
}
