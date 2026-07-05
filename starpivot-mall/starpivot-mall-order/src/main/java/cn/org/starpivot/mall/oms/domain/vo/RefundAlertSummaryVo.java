package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RefundAlertSummaryVo {

    private Long unreadCount;

    private List<RefundAlertItemVo> recentItems;

    @Data
    public static class RefundAlertItemVo {
        private Long id;
        private String orderSn;
        private String refundSn;
        private BigDecimal refund;
        private Integer refundChannel;
    }
}
