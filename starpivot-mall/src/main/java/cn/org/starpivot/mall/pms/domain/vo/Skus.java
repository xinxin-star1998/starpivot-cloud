package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * Skus请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class Skus {

  /**
   * SKU ID
   */
  private Long skuId;
  /**
   * attr
   */
  private List<Attr> attr;
  /**
   * sku名称
   */
  private String skuName;
  /**
   * price
   */
  private BigDecimal price;
  /**
   * sku Title
   */
  private String skuTitle;
  /**
   * sku Subtitle
   */
  private String skuSubtitle;
  /**
   * images
   */
  private List<Images> images;
  /**
   * descar
   */
  private List<String> descar;
  /**
   * full数量
   */
  private int fullCount;
  /**
   * discount
   */
  private BigDecimal discount;
  /**
   * 状态
   */
  private int countStatus;
  /**
   * full Price
   */
  private BigDecimal fullPrice;
  /**
   * reduce Price
   */
  private BigDecimal reducePrice;
  /**
   * 状态
   */
  private int priceStatus;
  /**
   * member Price
   */
  private List<MemberPrice> memberPrice;

  /** 发布时初始入库数量（仅新增商品时生效） */
  private Integer initialStock;

  /** 库存预警阈值 */
  private Integer stockWarning;

  /** C 端可售库存（WMS 可售 - Redis 预扣） */
  private Integer availableStock;

}
