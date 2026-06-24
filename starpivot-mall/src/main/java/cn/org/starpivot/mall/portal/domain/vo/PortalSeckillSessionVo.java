package cn.org.starpivot.mall.portal.domain.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PortalSeckillSessionVo {

    private Long id;

    private String name;

    /** HH:mm */
    private String startLabel;

    /** HH:mm */
    private String endLabel;

    /** ongoing | upcoming | ended */
    private String state;

    private List<PortalHomeProductVo> products = new ArrayList<>();
}
