package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_subject")
public class SmsHomeSubject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String title;

    private String subTitle;

    private Integer status;

    private String url;

    private Integer sort;

    private String img;

}
