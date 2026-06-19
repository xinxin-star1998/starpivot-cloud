package cn.org.starpivot.common.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果包装类
 */
@Data
@NoArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long totalPage;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 列表数据
     */
    private List<T> list;

    public PageResponse(List<T> list, Long totalCount) {
        this.list = list;
        this.totalCount = totalCount;
    }

    public PageResponse(Long pageNum, Long pageSize, List<T> list, Long totalCount) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.totalCount = totalCount;
        this.totalPage = (totalCount + pageSize - 1) / pageSize;
    }

    // 添加缺失的getter/setter方法
    public long getTotal() {
        return totalCount != null ? totalCount : 0;
    }

    public void setTotal(long total) {
        this.totalCount = total;
    }

    public List<T> getRows() {
        return list;
    }

    public void setRows(List<T> rows) {
        this.list = rows;
    }

    public long getPageNum() {
        return pageNum != null ? pageNum : 1;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize != null ? pageSize : 10;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageCount(long pageCount) {
        this.totalPage = pageCount;
    }
}