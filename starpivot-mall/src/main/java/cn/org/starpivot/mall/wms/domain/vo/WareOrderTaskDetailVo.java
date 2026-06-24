package cn.org.starpivot.mall.wms.domain.vo;

import lombok.Data;

@Data
public class WareOrderTaskDetailVo {

    private Long id;

    private Long skuId;

    private String skuName;

    private Integer skuNum;

    private Long taskId;

    private Long wareId;

    private Integer lockStatus;
}
