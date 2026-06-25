package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员价格请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberPrice {

  /**
   * 主键 ID
   */
  private Long id;
  /**
   * 名称
   */
  private String name;
  /**
   * price
   */
  private BigDecimal price;

}
