package cn.org.starpivot.system.domain.dto;

import lombok.Data;

/**
 * 参数配置新增/编辑 DTO。
 * <p>
 * 用于系统参数创建与更新接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class SysConfigDTO {

    /**
     * 参数主键（编辑时必填）
     */
    private Long configId;

    /**
     * 参数名称
     */
    private String configName;

    /**
     * 参数键名
     */
    private String configKey;

    /**
     * 参数键值
     */
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    private String configType;

    /**
     * 备注
     */
    private String remark;
}
