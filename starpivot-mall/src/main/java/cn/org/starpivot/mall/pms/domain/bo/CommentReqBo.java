package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品评论分页查询（pms_spu_comment） */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentReqBo extends PageReqBo {

    private Long spuId;

    private Long skuId;

    /** 会员昵称模糊 */
    private String memberNickName;

    /** 0-隐藏 1-显示 */
    private Integer showStatus;

    private Integer star;
}
