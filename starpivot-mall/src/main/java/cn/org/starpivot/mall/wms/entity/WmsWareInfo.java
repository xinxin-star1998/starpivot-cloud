package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_ware_info")
public class WmsWareInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private String areacode;

}
