package cn.org.starpivot.mall.wms.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地址DTO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class AddressQueryDTO extends PageReqBo {

    /** 地区编码（精确） */
    /**
     * code
     */
    private String code;

    /** 父级编码（精确） */
    /**
     * parent Code
     */
    private String parentCode;

    /** 地区名称（模糊） */
    /**
     * 名称
     */
    private String name;

    /** 层级：0-省 1-市 2-区县 3-乡镇 */
    /**
     * level
     */
    private Long level;
}
