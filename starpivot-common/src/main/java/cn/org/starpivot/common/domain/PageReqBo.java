package cn.org.starpivot.common.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询请求基类，封装页码与每页条数。
 * <p>
 * 供 Controller / Service 层接收前端分页参数，配合 {@link cn.org.starpivot.common.entity.PageResponse} 返回列表数据。
 * </p>
 */
@Data
public class PageReqBo {
    private static final int MAX_PAGE_NUM = 1000;
    private static final int MAX_PAGE_SIZE = 1000;

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "页码不能小于1")
    @Max(value = MAX_PAGE_NUM, message = "页码不能超过" + MAX_PAGE_NUM)
    private Long pageNum = 1L;

    /** 每页记录数 */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页数量不能超过" + MAX_PAGE_SIZE)
    private Long pageSize = 10L;
}
