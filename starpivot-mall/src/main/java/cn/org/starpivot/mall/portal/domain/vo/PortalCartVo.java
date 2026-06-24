package cn.org.starpivot.mall.portal.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class PortalCartVo {

    private List<PortalCartItemVo> items;

    private Integer checkedCount;

    private BigDecimal checkedAmount;
}
