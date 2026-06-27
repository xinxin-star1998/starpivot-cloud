package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ap_template_route")
public class ApTemplateRoute {

    @TableId(value = "route_id", type = IdType.AUTO)
    private Long routeId;
    private Long templateId;
    private Long fromStepId;
    private Integer priority;
    private String conditionExpr;
    private Long toStepId;
}
