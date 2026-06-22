package cn.org.starpivot.common.entity;

import lombok.Data;

import java.util.List;

/**
 * 分页查询响应体，封装总记录数、当前页数据及分页元信息。
 *
 * @param <T> 列表元素类型
 * @see cn.org.starpivot.common.domain.PageReqBo
 */
@Data
public class PageResponse<T> {

    /** 符合条件的总记录数 */
    private Long total;

    /** 当前页数据列表 */
    private List<T> rows;

    /** 当前页码 */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /** 总页数 */
    private Long pageCount;
}
