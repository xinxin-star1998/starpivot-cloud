package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 砍价活动查询请求 BO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BargainActivityReqBo extends PageReqBo {

    /** 活动标题（模糊匹配） */
    private String title;

    /** 上下线状态 */
    private Integer status;

    /** SKU ID */
    private Long skuId;
}
