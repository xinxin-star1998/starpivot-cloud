package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_subject_spu")
public class SmsHomeSubjectSpu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long subjectId;

    private Long spuId;

    private Integer sort;

}
