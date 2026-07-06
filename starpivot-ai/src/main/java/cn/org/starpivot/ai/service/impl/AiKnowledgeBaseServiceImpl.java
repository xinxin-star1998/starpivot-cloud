package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseSaveDto;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeBase;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeChunk;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeBaseVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeBaseMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeDocumentMapper;
import cn.org.starpivot.ai.service.AiKnowledgeBaseService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiKnowledgeBaseServiceImpl implements AiKnowledgeBaseService {

    private static final String STATUS_NORMAL = "0";

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;
    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiKnowledgeBaseVo> pageList(AiKnowledgeBaseQueryDto query) {
        Page<AiKnowledgeBase> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiKnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getKbName()), AiKnowledgeBase::getKbName, query.getKbName())
                .eq(StringUtils.hasText(query.getStatus()), AiKnowledgeBase::getStatus, query.getStatus())
                .orderByDesc(AiKnowledgeBase::getUpdateTime);
        Page<AiKnowledgeBase> result = aiKnowledgeBaseMapper.selectPage(page, wrapper);
        PageResponse<AiKnowledgeBaseVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiKnowledgeBaseVo> listEnabled() {
        return aiKnowledgeBaseMapper.selectList(new LambdaQueryWrapper<AiKnowledgeBase>()
                        .eq(AiKnowledgeBase::getStatus, STATUS_NORMAL)
                        .orderByDesc(AiKnowledgeBase::getUpdateTime))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeBaseVo getById(Long kbId) {
        return toVo(requireKb(kbId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiKnowledgeBaseSaveDto dto) {
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        AiKnowledgeBase entity;
        if (dto.getKbId() != null) {
            entity = requireKb(dto.getKbId());
        } else {
            entity = new AiKnowledgeBase();
            entity.setCreateBy(operator);
            entity.setCreateTime(now);
        }
        entity.setKbName(dto.getKbName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setTopK(dto.getTopK() != null ? dto.getTopK() : 5);
        entity.setChunkSize(dto.getChunkSize() != null ? dto.getChunkSize() : 600);
        entity.setChunkOverlap(dto.getChunkOverlap() != null ? dto.getChunkOverlap() : 80);
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : STATUS_NORMAL);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);
        if (dto.getKbId() != null) {
            aiKnowledgeBaseMapper.updateById(entity);
        } else {
            aiKnowledgeBaseMapper.insert(entity);
        }
        return entity.getKbId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long kbId) {
        requireKb(kbId);
        List<AiKnowledgeDocument> docs = aiKnowledgeDocumentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKbId, kbId));
        for (AiKnowledgeDocument doc : docs) {
            aiKnowledgeChunkMapper.delete(new LambdaQueryWrapper<AiKnowledgeChunk>()
                    .eq(AiKnowledgeChunk::getDocId, doc.getDocId()));
            aiKnowledgeDocumentMapper.deleteById(doc.getDocId());
        }
        aiKnowledgeBaseMapper.deleteById(kbId);
    }

    private AiKnowledgeBase requireKb(Long kbId) {
        AiKnowledgeBase entity = aiKnowledgeBaseMapper.selectById(kbId);
        if (entity == null) {
            throw new BizException("知识库不存在");
        }
        return entity;
    }

    private AiKnowledgeBaseVo toVo(AiKnowledgeBase entity) {
        AiKnowledgeBaseVo vo = new AiKnowledgeBaseVo();
        vo.setKbId(entity.getKbId());
        vo.setKbName(entity.getKbName());
        vo.setDescription(entity.getDescription());
        vo.setTopK(entity.getTopK());
        vo.setChunkSize(entity.getChunkSize());
        vo.setChunkOverlap(entity.getChunkOverlap());
        vo.setStatus(entity.getStatus());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String currentOperator() {
        String username = SecurityContextUtils.getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }
}
