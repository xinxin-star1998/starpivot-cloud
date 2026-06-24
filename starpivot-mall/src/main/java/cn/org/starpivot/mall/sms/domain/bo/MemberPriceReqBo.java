package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberPriceReqBo extends PageReqBo {

    private Long skuId;
    private Long memberLevelId;
}
