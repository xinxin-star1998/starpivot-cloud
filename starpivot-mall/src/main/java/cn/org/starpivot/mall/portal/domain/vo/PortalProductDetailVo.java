package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalProductDetailVo extends ProductVo {

    private String brandName;
}
