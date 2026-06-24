package cn.org.starpivot.mall.portal.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PortalProductListVo {

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
}
