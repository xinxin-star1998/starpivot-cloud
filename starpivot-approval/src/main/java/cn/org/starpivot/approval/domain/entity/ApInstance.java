package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_instance")
public class ApInstance {

    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;
    private Long templateId;
    private String templateCode;
    private String bizModule;
    private String bizType;
    private String bizKey;
    private String title;
    private Long starterId;
    private String status;
    private Long currentStepId;
    private String contextJson;
    @TableField(exist = false)
    private String runningFlag;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
