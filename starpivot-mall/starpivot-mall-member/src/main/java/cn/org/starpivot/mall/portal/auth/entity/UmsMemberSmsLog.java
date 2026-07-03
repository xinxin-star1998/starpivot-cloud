package cn.org.starpivot.mall.portal.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C 端短信发送流水。
 */
@Data
@TableName("ums_member_sms_log")
public class UmsMemberSmsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String mobile;

    private String scene;

    private String provider;

    /** 0失败 1成功 */
    private Integer sendStatus;

    private String ip;

    private LocalDateTime createTime;
}
