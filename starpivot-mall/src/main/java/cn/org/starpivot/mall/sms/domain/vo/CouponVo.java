package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CouponVo {

    private Long id;
    private Integer couponType;
    private String couponImg;
    private String couponName;
    private Integer num;
    private BigDecimal amount;
    private Integer perLimit;
    private BigDecimal minPoint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer useType;
    private String note;
    private Integer publishCount;
    private Integer useCount;
    private Integer receiveCount;
    private LocalDateTime enableStartTime;
    private LocalDateTime enableEndTime;
    private String code;
    private Integer memberLevel;
    private Integer publish;
    private List<CouponSpuVo> spuList;
    private List<CouponCategoryVo> categoryList;
}
