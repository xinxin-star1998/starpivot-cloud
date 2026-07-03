package cn.org.starpivot.api.product.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryBrandRelationDto implements Serializable {

    private Long catelogId;
    private Long brandId;
}
