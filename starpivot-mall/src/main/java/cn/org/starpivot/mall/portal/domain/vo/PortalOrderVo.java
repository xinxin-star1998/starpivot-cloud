package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.mall.oms.domain.vo.OmsOrderItemVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PortalOrderVo {

    private Long id;

    private String orderSn;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private String receiverName;

    private String receiverPhone;

    private String receiverProvince;

    private String receiverCity;

    private String receiverRegion;

    private String receiverDetailAddress;

    private String note;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private List<OmsOrderItemVo> orderItemList;
}
