package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_template_bind")
public class ApTemplateBind {

    @TableId(value = "bind_id", type = IdType.AUTO)
    private Long bindId;
    private String bizModule;
    private String bizType;
    private String matchExpr;
    private String templateCode;
    private Integer priority;
    private String status;
    private LocalDateTime createTime;
}
