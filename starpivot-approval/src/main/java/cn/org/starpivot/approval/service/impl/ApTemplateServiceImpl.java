package cn.org.starpivot.approval.service.impl;

import cn.org.starpivot.approval.constant.ApprovalConstants;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindSaveDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateSaveDto;
import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateBind;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.approval.domain.vo.ApTemplateDetailVo;
import cn.org.starpivot.approval.mapper.ApTemplateBindMapper;
import cn.org.starpivot.approval.mapper.ApTemplateMapper;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApTemplateServiceImpl implements ApTemplateService {

    private final ApTemplateMapper templateMapper;
    private final ApTemplateStepMapper stepMapper;
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
            step.setCreateTime(now);
            stepMapper.insert(step);
        }
        return template.getTemplateId();
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
