package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CouponSaveBo {

    private Long id;

    private Integer couponType;

    @Size(max = 512, message = "优惠券图片长度不能超过512")
    private String couponImg;

    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 128, message = "优惠券名称长度不能超过128")
    private String couponName;

    private Integer num;
    private BigDecimal amount;
    private Integer perLimit;
    private BigDecimal minPoint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer useType;

    @Size(max = 512, message = "备注长度不能超过512")
    private String note;

    private Integer publishCount;
    private Integer useCount;
    private Integer receiveCount;
    private LocalDateTime enableStartTime;
    private LocalDateTime enableEndTime;

    @Size(max = 64, message = "优惠码长度不能超过64")
    private String code;

    private Integer memberLevel;
    private Integer publish;

    /** useType=2 时关联 SPU */
    private List<CouponSpuBo> spuList;

    /** useType=1 时关联分类 */
    private List<CouponCategoryBo> categoryList;
}
