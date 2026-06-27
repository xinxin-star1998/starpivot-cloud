package cn.org.starpivot.approval.service.engine;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateBind;
import cn.org.starpivot.approval.domain.entity.ApTemplateRoute;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.approval.mapper.ApTemplateBindMapper;
import cn.org.starpivot.approval.mapper.ApTemplateMapper;
import cn.org.starpivot.approval.mapper.ApTemplateRouteMapper;
import cn.org.starpivot.approval.mapper.ApTemplateStepMapper;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 模板解析：显式 templateCode 或 ap_template_bind 自动匹配。
 */
@Component
@RequiredArgsConstructor
public class TemplateResolver {

    private final ApTemplateMapper templateMapper;
    private final ApTemplateBindMapper bindMapper;
    private final ApTemplateStepMapper stepMapper;
    private final ApTemplateRouteMapper routeMapper;
    private final SpelEvaluator spelEvaluator;
    private final ObjectMapper objectMapper;

    public ResolvedTemplate resolve(String bizModule, String bizType, String templateCode, Map<String, Object> context) {
        ApTemplate template;
        if (StringUtils.hasText(templateCode)) {
            template = findPublishedTemplate(templateCode);
        } else {
            template = matchByBind(bizModule, bizType, context);
        }
        List<ApTemplateStep> steps = loadSteps(template.getTemplateId());
        if (steps.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批模板未配置步骤: " + template.getTemplateCode());
        }
        List<ApTemplateRoute> routes = routeMapper.selectList(new LambdaQueryWrapper<ApTemplateRoute>()
                .eq(ApTemplateRoute::getTemplateId, template.getTemplateId())
                .orderByAsc(ApTemplateRoute::getFromStepId, ApTemplateRoute::getPriority));
        return new ResolvedTemplate(template, steps, routes);
    }

    public List<ApTemplateStep> loadSteps(Long templateId) {
        return stepMapper.selectList(new LambdaQueryWrapper<ApTemplateStep>()
                .eq(ApTemplateStep::getTemplateId, templateId)
                .orderByAsc(ApTemplateStep::getStepOrder));
    }

    private ApTemplate findPublishedTemplate(String templateCode) {
        ApTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<ApTemplate>()
                .eq(ApTemplate::getTemplateCode, templateCode)
                .eq(ApTemplate::getStatus, ApprovalConstants.TEMPLATE_PUBLISHED)
                .orderByDesc(ApTemplate::getVersion)
                .last("LIMIT 1"));
        if (template == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "未找到已发布的审批模板: " + templateCode);
        }
        return template;
    }

    private ApTemplate matchByBind(String bizModule, String bizType, Map<String, Object> context) {
        List<ApTemplateBind> binds = bindMapper.selectList(new LambdaQueryWrapper<ApTemplateBind>()
                .eq(ApTemplateBind::getBizModule, bizModule)
                .eq(ApTemplateBind::getBizType, bizType)
                .eq(ApTemplateBind::getStatus, "0")
                .orderByAsc(ApTemplateBind::getPriority));
        for (ApTemplateBind bind : binds) {
            if (spelEvaluator.evaluateBoolean(bind.getMatchExpr(), context)) {
                return findPublishedTemplate(bind.getTemplateCode());
            }
        }
        throw new BizException(ErrorCode.PARAM_INVALID, "无匹配的审批模板绑定: " + bizModule + "/" + bizType);
    }

    public Map<String, Object> parseContext(String contextJson) {
        if (!StringUtils.hasText(contextJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(contextJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批上下文 JSON 无效");
        }
    }

    public String writeContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批上下文序列化失败");
        }
    }

    public record ResolvedTemplate(
            ApTemplate template,
            List<ApTemplateStep> steps,
            List<ApTemplateRoute> routes) {

        public ApTemplateStep firstStep() {
            return steps.stream().min(Comparator.comparing(ApTemplateStep::getStepOrder)).orElseThrow();
        }

        public ApTemplateStep findStep(Long stepId) {
            return steps.stream()
                    .filter(s -> s.getStepId().equals(stepId))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "步骤不存在"));
        }

        public ApTemplateStep nextLinearStep(ApTemplateStep current) {
            return steps.stream()
                    .filter(s -> s.getStepOrder() > current.getStepOrder())
                    .min(Comparator.comparing(ApTemplateStep::getStepOrder))
                    .orElse(null);
        }
    }
}
