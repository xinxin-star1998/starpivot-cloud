package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_ware_order_task_detail")
public class WmsWareOrderTaskDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private String skuName;

    private Integer skuNum;

    private Long taskId;

    private Long wareId;

    private Integer lockStatus;

}
