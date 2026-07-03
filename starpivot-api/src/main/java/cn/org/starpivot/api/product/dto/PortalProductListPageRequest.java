package cn.org.starpivot.api.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PortalProductListPageRequest implements Serializable {

    private List<Long> orderedSpuIds;
    private int pageNum = 1;
    private int pageSize = 10;
}
