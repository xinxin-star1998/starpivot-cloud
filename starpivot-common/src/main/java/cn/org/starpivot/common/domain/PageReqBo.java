package cn.org.starpivot.common.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageReqBo {
    private static final int MAX_PAGE_NUM = 1000;
    private static final int MAX_PAGE_SIZE = 1000;

    @Min(value = 1, message = "页码不能小于1")
    @Max(value = MAX_PAGE_NUM, message = "页码不能超过" + MAX_PAGE_NUM)
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页数量不能超过" + MAX_PAGE_SIZE)
    private Integer pageSize = 10;
}
