package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MemberPriceVo {

    private Long id;
    private Long skuId;
    private String skuName;
    private Long memberLevelId;
    private String memberLevelName;
    private BigDecimal memberPrice;
    private Integer addOther;
}
