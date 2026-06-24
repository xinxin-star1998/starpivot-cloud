package cn.org.starpivot.mall.ums.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MemberLevelSaveBo {

    private Long id;

    @NotBlank(message = "等级名称不能为空")
    @Size(max = 64, message = "等级名称长度不能超过64")
    private String name;

    private Integer growthPoint;
    private Integer defaultStatus;
    private BigDecimal freeFreightPoint;
    private Integer commentGrowthPoint;
    private Integer privilegeFreeFreight;
    private Integer privilegeMemberPrice;
    private Integer privilegeBirthday;

    @Size(max = 512, message = "备注长度不能超过512")
    private String note;
}
