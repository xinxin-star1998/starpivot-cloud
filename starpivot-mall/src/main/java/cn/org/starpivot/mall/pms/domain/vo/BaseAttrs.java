package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

@Data
public class BaseAttrs {

  private Long attrId;
  private String attrName;
  private String attrValues;
  private int showDesc;

}
