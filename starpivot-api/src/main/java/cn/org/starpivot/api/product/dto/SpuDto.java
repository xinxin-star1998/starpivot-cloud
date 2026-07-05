package cn.org.starpivot.api.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SpuDto implements Serializable {

    private Long id;
    private String spuName;
    private Long catalogId;
    private Long brandId;
    private Integer publishStatus;
    private String auditStatus;
    /** SPU 重量（kg），用于 TMS 运费计算 */
    private BigDecimal weight;
}
