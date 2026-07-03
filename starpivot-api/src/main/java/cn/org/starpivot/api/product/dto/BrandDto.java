package cn.org.starpivot.api.product.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BrandDto implements Serializable {

    private Long brandId;
    private String name;
    private String logo;
    private Integer sort;
    private Integer showStatus;
}
