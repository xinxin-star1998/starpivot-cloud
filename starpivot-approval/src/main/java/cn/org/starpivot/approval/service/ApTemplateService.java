package cn.org.starpivot.approval.service;

import cn.org.starpivot.approval.domain.dto.ApTemplateBindQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindSaveDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateSaveDto;
import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateBind;
import cn.org.starpivot.approval.domain.vo.ApTemplateDetailVo;
import cn.org.starpivot.common.entity.PageResponse;

import java.util.List;

public interface ApTemplateService {

    PageResponse<ApTemplate> pageList(ApTemplateQueryDto query);

    ApTemplateDetailVo getDetail(Long templateId);

    Long save(ApTemplateSaveDto dto, String operator);

    void publish(Long templateId, String operator);

    void disable(Long templateId, String operator);

    PageResponse<ApTemplateBind> pageBindList(ApTemplateBindQueryDto query);

    void saveBind(ApTemplateBindSaveDto dto);

    void deleteBind(List<Long> bindIds);
}
