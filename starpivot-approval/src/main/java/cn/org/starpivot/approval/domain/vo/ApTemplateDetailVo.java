package cn.org.starpivot.approval.domain.vo;

import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import lombok.Data;

import java.util.List;

@Data
public class ApTemplateDetailVo {

    private ApTemplate template;
    private List<ApTemplateStep> steps;
}
