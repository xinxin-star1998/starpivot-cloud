package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 仓库查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsWareInfoReqBo extends PageReqBo {

    /**
     * 主键 ID
     */
    private Long id;

    /** 仓库名称（模糊） */
    /**
     * 名称
     */
    private String name;

    /** 详细地址（模糊） */
    /**
     * address
     */
    private String address;

    /** 区域编码 */
    /**
     * areacode
     */
    private String areacode;
}
