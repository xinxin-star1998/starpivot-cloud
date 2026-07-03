package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MemberLevelDto implements Serializable {

    private Long id;
    private String name;
    private Integer growthPoint;
    private BigDecimal freeFreightPoint;
    private BigDecimal priviledgeDiscount;
    private Integer priviledgeFreeFreight;
    private Integer priviledgeSignIn;
    private String priviledgeComment;
    private String priviledgePromotion;
    private Integer priviledgeMemberPrice;
    private Integer priviledgeBirthday;
}
