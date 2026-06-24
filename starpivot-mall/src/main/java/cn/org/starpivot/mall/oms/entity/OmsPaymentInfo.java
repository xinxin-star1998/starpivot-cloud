package cn.org.starpivot.mall.oms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_payment_info")
public class OmsPaymentInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderSn;

    private Long orderId;

    private String alipayTradeNo;

    private BigDecimal totalAmount;

    private String subject;

    private String paymentStatus;

    private LocalDateTime createTime;

    private LocalDateTime confirmTime;

    private String callbackContent;

    private LocalDateTime callbackTime;

}
