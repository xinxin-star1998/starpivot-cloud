package cn.org.starpivot.tms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_carrier")
public class TmsCarrier {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String carrierCode;
    private String carrierName;
    private String kuaidi100Com;
    private Integer sortOrder;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private String delFlag;
}
