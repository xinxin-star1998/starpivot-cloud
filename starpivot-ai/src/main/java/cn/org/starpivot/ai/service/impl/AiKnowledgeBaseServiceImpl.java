package cn.org.starpivot.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseSaveDto;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeBase;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeChunk;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeBaseVo;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeReindexResultVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeBaseMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeDocumentMapper;
import cn.org.starpivot.ai.service.AiIndexService;
import cn.org.starpivot.ai.service.AiKnowledgeBaseService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
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
public class AiKnowledgeBaseServiceImpl implements AiKnowledgeBaseService {

    private static final String STATUS_NORMAL = "0";

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;
    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final AiIndexService aiIndexService;

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
        List<AiKnowledgeBaseVo> rows = result.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        fillStats(rows);
        response.setRows(rows);
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
        AiKnowledgeBaseVo vo = toVo(requireKb(kbId));
        fillStats(List.of(vo));
        return vo;
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

    @Override
    public AiKnowledgeReindexResultVo reindexAll(Long kbId) {
        requireKb(kbId);
        List<AiKnowledgeDocument> docs = aiKnowledgeDocumentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKbId, kbId)
                .eq(AiKnowledgeDocument::getStatus, STATUS_NORMAL)
                .orderByAsc(AiKnowledgeDocument::getDocId));
        int submitted = 0;
        int skipped = 0;
        for (AiKnowledgeDocument doc : docs) {
            boolean hasFile = "FILE".equals(doc.getSourceType()) && StringUtils.hasText(doc.getObjectName());
            boolean hasText = StringUtils.hasText(doc.getContent());
            if (!hasFile && !hasText) {
                skipped++;
                continue;
            }
            aiIndexService.forceResetIndexState(doc.getDocId());
            if (hasFile) {
                aiIndexService.submitFileIndex(doc.getDocId());
            } else {
                aiIndexService.submitTextIndex(doc.getDocId());
            }
            submitted++;
        }
        return AiKnowledgeReindexResultVo.builder().submitted(submitted).skipped(skipped).build();
    }

    private void fillStats(List<AiKnowledgeBaseVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> kbIds = rows.stream().map(AiKnowledgeBaseVo::getKbId).filter(id -> id != null).toList();
        if (kbIds.isEmpty()) {
            return;
        }
        List<AiKnowledgeDocument> docs = aiKnowledgeDocumentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .in(AiKnowledgeDocument::getKbId, kbIds)
                .select(
                        AiKnowledgeDocument::getKbId,
                        AiKnowledgeDocument::getChunkCount,
                        AiKnowledgeDocument::getIndexStatus));
        Map<Long, int[]> stats = new HashMap<>();
        for (AiKnowledgeDocument doc : docs) {
            int[] bucket = stats.computeIfAbsent(doc.getKbId(), key -> new int[5]);
            bucket[0]++; // docCount
            bucket[1] += doc.getChunkCount() != null ? doc.getChunkCount() : 0;
            String status = doc.getIndexStatus();
            if ("DONE".equals(status)) {
                bucket[2]++;
            } else if ("FAILED".equals(status)) {
                bucket[4]++;
            } else {
                bucket[3]++; // pending/processing/other
            }
        }
        for (AiKnowledgeBaseVo row : rows) {
            int[] bucket = stats.getOrDefault(row.getKbId(), new int[5]);
            row.setDocCount(bucket[0]);
            row.setChunkCount(bucket[1]);
            row.setIndexedCount(bucket[2]);
            row.setIndexingCount(bucket[3]);
            row.setFailedCount(bucket[4]);
        }
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
