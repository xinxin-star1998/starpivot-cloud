package cn.org.starpivot.system.domain.dto;

import lombok.Data;

/**
 * 参数配置分页查询 DTO。
 * <p>
 * 封装系统参数列表接口的筛选条件与分页参数。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class SysConfigQueryDTO {

    /**
     * 当前页码，默认第 1 页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认 10 条
     */
    private Integer pageSize = 10;

    /**
     * 参数名称（模糊匹配）
     */
    private String configName;

    /**
     * 参数键名（模糊匹配）
     */
    private String configKey;

    /**
     * 参数键值（模糊匹配）
     */
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    private String configType;
}
