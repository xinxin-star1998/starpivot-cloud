package cn.org.starpivot.mall.wms.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseVo {

    private Long id;

    private Long assigneeId;

    private String assigneeName;

    private String phone;

    private Integer priority;

    private Integer status;

    private Long wareId;

    private BigDecimal amount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<PurchaseDetailVo> details;
}
