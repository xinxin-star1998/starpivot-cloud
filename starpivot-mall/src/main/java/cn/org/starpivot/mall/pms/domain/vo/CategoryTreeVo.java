package cn.org.starpivot.mall.pms.domain.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CategoryTreeVo {

    private Long catId;
    private String name;
    private Long parentCid;
    private Long catLevel;
    private Long showStatus;
    private Long sort;
    private String icon;
    private String productUnit;
    private Long productCount;
    private List<CategoryTreeVo> children = new ArrayList<>();
}
