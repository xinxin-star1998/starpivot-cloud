package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品详情视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalProductDetailVo extends ProductVo {

    /**
     * brand名称
     */
    private String brandName;
}
