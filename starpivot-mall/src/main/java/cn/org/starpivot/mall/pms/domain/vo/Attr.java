package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

/**
 * 商品属性请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class Attr {

  /**
   * 属性 ID
   */
  private Long attrId;
  /**
   * 属性名称
   */
  private String attrName;
  /**
   * attr Value
   */
  private String attrValue;
}
