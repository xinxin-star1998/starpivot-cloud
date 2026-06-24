package cn.org.starpivot.mall.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_refund_info")
public class OmsRefundInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderReturnId;

    private BigDecimal refund;

    private String refundSn;

    private Integer refundStatus;

    private Integer refundChannel;

    private String refundContent;

}
