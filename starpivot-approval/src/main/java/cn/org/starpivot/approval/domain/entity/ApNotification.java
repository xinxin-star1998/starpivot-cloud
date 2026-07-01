package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_notification")
public class ApNotification {

    @TableId(value = "notify_id", type = IdType.AUTO)
    private Long notifyId;
    private Long userId;
    private String notifyType;
    private String title;
    private String content;
    private Long instanceId;
    private Long taskId;
    /** 0未读 1已读 */
    private String readFlag;
    private LocalDateTime createTime;
}
