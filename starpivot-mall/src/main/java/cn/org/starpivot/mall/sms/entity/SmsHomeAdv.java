package cn.org.starpivot.mall.sms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_adv")
public class SmsHomeAdv {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String pic;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer clickCount;

    private String url;

    private String note;

    private Integer sort;

    private Long publisherId;

    private Long authId;

}
