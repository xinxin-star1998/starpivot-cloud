package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_template_step")
public class ApTemplateStep {

    @TableId(value = "step_id", type = IdType.AUTO)
    private Long stepId;
    private Long templateId;
    private String stepCode;
    private String stepName;
    private Integer stepOrder;
    private String assigneeType;
    private String assigneeValue;
    private String approveMode;
    private String skipExpression;
    /** 超时小时数，null 表示不启用 */
    private Integer timeoutHours;
    /** AUTO_REJECT / AUTO_APPROVE */
    private String timeoutAction;
    private LocalDateTime createTime;
}
