package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_order_setting")
public class OmsOrderSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer flashOrderOvertime;

    private Integer normalOrderOvertime;

    private Integer confirmOvertime;

    private Integer finishOvertime;

    private Integer commentOvertime;

    private Integer memberLevel;

}
