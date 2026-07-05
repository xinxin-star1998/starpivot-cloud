package cn.org.starpivot.api.tms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FreightCalculateRequest implements Serializable {

    private String receiverProvince;
    private BigDecimal merchandiseAmount;
    private List<FreightLine> lines;

    @Data
    public static class FreightLine implements Serializable {
        private Long skuId;
        private Long spuId;
        private Integer quantity;
    }
}
