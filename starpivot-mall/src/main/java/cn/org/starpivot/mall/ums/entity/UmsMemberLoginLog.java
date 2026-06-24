package cn.org.starpivot.mall.ums.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_login_log")
public class UmsMemberLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private LocalDateTime createTime;

    private String ip;

    private String city;

    private Integer loginType;

}
