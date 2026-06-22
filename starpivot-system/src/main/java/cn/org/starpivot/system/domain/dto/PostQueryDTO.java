package cn.org.starpivot.system.domain.dto;

import lombok.Data;

/**
 * 岗位分页查询 DTO。
 * <p>
 * 封装岗位列表接口的筛选条件与分页参数。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class PostQueryDTO {

    /**
     * 当前页码，默认第 1 页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认 10 条
     */
    private Integer pageSize = 10;

    /**
     * 岗位编码（模糊匹配）
     */
    private String postCode;

    /**
     * 岗位名称（模糊匹配）
     */
    private String postName;

    /**
     * 状态（0正常 1停用）
     */
    private String status;
}
