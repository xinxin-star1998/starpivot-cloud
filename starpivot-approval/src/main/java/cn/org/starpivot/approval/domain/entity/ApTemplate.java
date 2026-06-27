package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_template")
public class ApTemplate {

    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;
    private String templateCode;
    private String templateName;
    private String bizModule;
    private Integer version;
    private String status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
