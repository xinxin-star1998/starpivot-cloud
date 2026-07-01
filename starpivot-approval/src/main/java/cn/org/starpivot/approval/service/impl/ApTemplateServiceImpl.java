package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindSaveDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateSaveDto;
import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateBind;
import cn.org.starpivot.approval.domain.entity.ApTemplateRoute;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.approval.domain.vo.ApTemplateDetailVo;
import cn.org.starpivot.approval.domain.vo.ApTemplateRouteVo;
import cn.org.starpivot.approval.mapper.ApTemplateBindMapper;
import cn.org.starpivot.approval.mapper.ApTemplateMapper;
import cn.org.starpivot.approval.mapper.ApTemplateRouteMapper;
import cn.org.starpivot.approval.mapper.ApTemplateStepMapper;
import cn.org.starpivot.approval.service.ApTemplateService;
import cn.org.starpivot.approval.service.engine.TemplateResolver;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApTemplateServiceImpl implements ApTemplateService {

    private final ApTemplateMapper templateMapper;
    private final ApTemplateStepMapper stepMapper;
    private final ApTemplateRouteMapper routeMapper;
    private final ApTemplateBindMapper bindMapper;
    private final TemplateResolver templateResolver;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApTemplate> pageList(ApTemplateQueryDto query) {
        Page<ApTemplate> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTemplateCode()), ApTemplate::getTemplateCode, query.getTemplateCode())
                .like(StringUtils.hasText(query.getTemplateName()), ApTemplate::getTemplateName, query.getTemplateName())
                .eq(StringUtils.hasText(query.getBizModule()), ApTemplate::getBizModule, query.getBizModule())
                .eq(StringUtils.hasText(query.getStatus()), ApTemplate::getStatus, query.getStatus())
                .orderByDesc(ApTemplate::getTemplateId);
        Page<ApTemplate> result = templateMapper.selectPage(page, wrapper);
        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ApTemplateDetailVo getDetail(Long templateId) {
        ApTemplate template = requireTemplate(templateId);
        ApTemplateDetailVo vo = new ApTemplateDetailVo();
        vo.setTemplate(template);
        vo.setSteps(templateResolver.loadSteps(templateId));
        vo.setRoutes(loadRouteVos(templateId, vo.getSteps()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(ApTemplateSaveDto dto, String operator) {
        LocalDateTime now = LocalDateTime.now();
        ApTemplate template;
        if (dto.getTemplateId() != null) {
            template = requireTemplate(dto.getTemplateId());
            if (ApprovalConstants.TEMPLATE_PUBLISHED.equals(template.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "已发布模板不可编辑，请复制新版本");
            }
            template.setTemplateName(dto.getTemplateName());
            template.setBizModule(dto.getBizModule());
            template.setRemark(dto.getRemark());
            template.setUpdateBy(operator);
            template.setUpdateTime(now);
            templateMapper.updateById(template);
            stepMapper.delete(new LambdaQueryWrapper<ApTemplateStep>().eq(ApTemplateStep::getTemplateId, template.getTemplateId()));
            routeMapper.delete(new LambdaQueryWrapper<ApTemplateRoute>().eq(ApTemplateRoute::getTemplateId, template.getTemplateId()));
        } else {
            template = new ApTemplate();
            template.setTemplateCode(dto.getTemplateCode());
            template.setTemplateName(dto.getTemplateName());
            template.setBizModule(dto.getBizModule());
            template.setVersion(1);
            template.setStatus(ApprovalConstants.TEMPLATE_DRAFT);
            template.setRemark(dto.getRemark());
            template.setCreateBy(operator);
            template.setCreateTime(now);
            templateMapper.insert(template);
        }
        Map<String, Long> stepCodeToId = new HashMap<>();
        for (ApTemplateSaveDto.ApTemplateStepDto stepDto : dto.getSteps()) {
            ApTemplateStep step = new ApTemplateStep();
            step.setTemplateId(template.getTemplateId());
            step.setStepCode(stepDto.getStepCode());
            step.setStepName(stepDto.getStepName());
            step.setStepOrder(stepDto.getStepOrder());
            step.setAssigneeType(stepDto.getAssigneeType());
            step.setAssigneeValue(stepDto.getAssigneeValue());
            step.setApproveMode(StringUtils.hasText(stepDto.getApproveMode())
                    ? stepDto.getApproveMode() : ApprovalConstants.APPROVE_MODE_ANY);
            step.setSkipExpression(stepDto.getSkipExpression());
            step.setTimeoutHours(stepDto.getTimeoutHours());
            step.setTimeoutAction(StringUtils.hasText(stepDto.getTimeoutAction())
                    ? stepDto.getTimeoutAction() : ApprovalConstants.TIMEOUT_ACTION_REJECT);
            step.setCreateTime(now);
            stepMapper.insert(step);
            stepCodeToId.put(step.getStepCode(), step.getStepId());
        }
        saveRoutes(template.getTemplateId(), dto.getRoutes(), stepCodeToId);
        return template.getTemplateId();
    }

    private void saveRoutes(Long templateId, List<ApTemplateSaveDto.ApTemplateRouteDto> routes,
                            Map<String, Long> stepCodeToId) {
        if (routes == null || routes.isEmpty()) {
            return;
        }
        for (ApTemplateSaveDto.ApTemplateRouteDto routeDto : routes) {
            if (!StringUtils.hasText(routeDto.getFromStepCode()) || !StringUtils.hasText(routeDto.getToStepCode())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "路由的起止步骤编码不能为空");
            }
            Long fromStepId = stepCodeToId.get(routeDto.getFromStepCode().trim());
            Long toStepId = stepCodeToId.get(routeDto.getToStepCode().trim());
            if (fromStepId == null || toStepId == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "路由引用了不存在的步骤编码");
            }
            ApTemplateRoute route = new ApTemplateRoute();
            route.setTemplateId(templateId);
            route.setFromStepId(fromStepId);
            route.setToStepId(toStepId);
            route.setPriority(routeDto.getPriority() != null ? routeDto.getPriority() : 0);
            route.setConditionExpr(routeDto.getConditionExpr());
            routeMapper.insert(route);
        }
    }

    private List<ApTemplateRouteVo> loadRouteVos(Long templateId, List<ApTemplateStep> steps) {
        Map<Long, ApTemplateStep> stepById = steps.stream()
                .collect(Collectors.toMap(ApTemplateStep::getStepId, s -> s, (a, b) -> a));
        List<ApTemplateRoute> routes = routeMapper.selectList(new LambdaQueryWrapper<ApTemplateRoute>()
                .eq(ApTemplateRoute::getTemplateId, templateId)
                .orderByAsc(ApTemplateRoute::getFromStepId, ApTemplateRoute::getPriority));
        return routes.stream().map(route -> {
            ApTemplateRouteVo vo = new ApTemplateRouteVo();
            vo.setRouteId(route.getRouteId());
            vo.setPriority(route.getPriority());
            vo.setConditionExpr(route.getConditionExpr());
            ApTemplateStep from = stepById.get(route.getFromStepId());
            ApTemplateStep to = stepById.get(route.getToStepId());
            if (from != null) {
                vo.setFromStepCode(from.getStepCode());
                vo.setFromStepName(from.getStepName());
            }
            if (to != null) {
                vo.setToStepCode(to.getStepCode());
                vo.setToStepName(to.getStepName());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long templateId, String operator) {
        ApTemplate template = requireTemplate(templateId);
        long stepCount = stepMapper.selectCount(new LambdaQueryWrapper<ApTemplateStep>()
                .eq(ApTemplateStep::getTemplateId, templateId));
        if (stepCount == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "模板未配置步骤，无法发布");
        }
        ApTemplate patch = new ApTemplate();
        patch.setTemplateId(templateId);
        patch.setStatus(ApprovalConstants.TEMPLATE_PUBLISHED);
        patch.setUpdateBy(operator);
        patch.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long templateId, String operator) {
        requireTemplate(templateId);
        ApTemplate patch = new ApTemplate();
        patch.setTemplateId(templateId);
        patch.setStatus(ApprovalConstants.TEMPLATE_DISABLED);
        patch.setUpdateBy(operator);
        patch.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(patch);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApTemplateBind> pageBindList(ApTemplateBindQueryDto query) {
        Page<ApTemplateBind> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApTemplateBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getBizModule()), ApTemplateBind::getBizModule, query.getBizModule())
                .eq(StringUtils.hasText(query.getBizType()), ApTemplateBind::getBizType, query.getBizType())
                .orderByAsc(ApTemplateBind::getPriority);
        Page<ApTemplateBind> result = bindMapper.selectPage(page, wrapper);
        return toPageResponse(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBind(ApTemplateBindSaveDto dto) {
        ApTemplateBind bind;
        if (dto.getBindId() != null) {
            bind = bindMapper.selectById(dto.getBindId());
            if (bind == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "绑定规则不存在");
            }
        } else {
            bind = new ApTemplateBind();
            bind.setCreateTime(LocalDateTime.now());
        }
        bind.setBizModule(dto.getBizModule());
        bind.setBizType(dto.getBizType());
        bind.setMatchExpr(dto.getMatchExpr());
        bind.setTemplateCode(dto.getTemplateCode());
        bind.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        bind.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "0");
        if (dto.getBindId() != null) {
            bindMapper.updateById(bind);
        } else {
            bindMapper.insert(bind);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBind(List<Long> bindIds) {
        if (bindIds == null || bindIds.isEmpty()) {
            return;
        }
        bindMapper.deleteBatchIds(bindIds);
    }

    private ApTemplate requireTemplate(Long templateId) {
        ApTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "模板不存在");
        }
        return template;
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setRows(page.getRecords());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setPageCount(page.getPages());
        return response;
    }
}
