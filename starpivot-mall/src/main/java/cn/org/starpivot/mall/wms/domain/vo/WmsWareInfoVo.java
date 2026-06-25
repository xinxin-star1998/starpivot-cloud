package cn.org.starpivot.mall.wms.domain.vo;

import lombok.Data;

/**
 * 仓库视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class WmsWareInfoVo {
    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * address
     */
    private String address;
    /**
     * areacode
     */
    private String areacode;
}
