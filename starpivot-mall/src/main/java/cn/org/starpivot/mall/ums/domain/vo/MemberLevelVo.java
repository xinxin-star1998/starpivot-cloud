package cn.org.starpivot.mall.ums.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MemberLevelVo {

    private Long id;
    private String name;
    private Integer growthPoint;
    private Integer defaultStatus;
    private BigDecimal freeFreightPoint;
    private Integer commentGrowthPoint;
    private Integer privilegeFreeFreight;
    private Integer privilegeMemberPrice;
    private Integer privilegeBirthday;
    private String note;
}
