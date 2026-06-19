package cn.org.starpivot.system.domain.dto;

import lombok.Data;

/**
 * 字典数据查询DTO
 *
 * @author stardust
 * @since 2024-01-01
 */
@Data
public class SysDictDataQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    private String status;
}