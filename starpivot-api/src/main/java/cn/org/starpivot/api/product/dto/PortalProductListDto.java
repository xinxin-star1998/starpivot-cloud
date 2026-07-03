package cn.org.starpivot.api.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PortalProductListDto implements Serializable {

    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private String brandName;
    private BigDecimal price;
    private String coverImg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private Long commentCount;
    private BigDecimal avgStar;
}
