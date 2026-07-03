package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * Bounds请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class Bounds {

  /**
   * buy Bounds
   */
  private BigDecimal buyBounds;
  /**
   * grow Bounds
   */
  private BigDecimal growBounds;

}
