package cn.org.starpivot.api.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeDto implements Serializable {

    private Long catId;
    private String name;
    private Long parentCid;
    private Long catLevel;
    private Long showStatus;
    private Long sort;
    private String icon;
    private String productUnit;
    private Long productCount;
    private List<CategoryTreeDto> children = new ArrayList<>();
}
