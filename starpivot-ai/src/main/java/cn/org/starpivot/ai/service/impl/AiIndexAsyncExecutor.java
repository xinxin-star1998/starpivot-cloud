package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.entity.AiIndexTask;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeBase;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeChunk;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;
import cn.org.starpivot.ai.mapper.AiIndexTaskMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeBaseMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeDocumentMapper;
import cn.org.starpivot.ai.rag.EmbeddingService;
import cn.org.starpivot.ai.rag.VectorUtils;
import cn.org.starpivot.ai.rag.loader.DocumentLoaderService;
import cn.org.starpivot.ai.rag.loader.ParseResult;
import cn.org.starpivot.ai.rag.splitter.ChunkResult;
import cn.org.starpivot.ai.rag.splitter.RagChunkService;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.FileStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIndexAsyncExecutor {

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;
    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final AiIndexTaskMapper aiIndexTaskMapper;
    private final RagChunkService ragChunkService;
    private final EmbeddingService embeddingService;
    private final DocumentLoaderService documentLoaderService;
    private final ObjectProvider<FileStorageService> fileStorageServiceProvider;
    private final ObjectMapper objectMapper;

    @Async("indexTaskExecutor")
    public void executeAsync(Long docId, Long taskId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc == null) {
            markFailed(taskId, docId, "文档不存在");
            return;
        }
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(doc.getKbId());
        if (kb == null) {
            markFailed(taskId, docId, "知识库不存在");
            return;
        }

        updateTaskStatus(taskId, AiIndexServiceImpl.STATUS_RUNNING);
        updateDocIndexStatus(docId, AiIndexServiceImpl.INDEX_PROCESSING, null);

        try {
            ParseResult parseResult = loadParseResult(doc);
            if (!parseResult.isSuccess()) {
                throw new BizException(parseResult.getErrorMsg());
            }

            int chunkSize = kb.getChunkSize() != null ? kb.getChunkSize() : 600;
            int overlap = kb.getChunkOverlap() != null ? kb.getChunkOverlap() : 80;
            List<ChunkResult> chunks = ragChunkService.chunk(parseResult, chunkSize, overlap);
            if (chunks.isEmpty()) {
                throw new BizException("分块结果为空，文档可能无有效文本内容");
            }

            int newVersion = (doc.getDocVersion() == null ? 1 : doc.getDocVersion() + 1);
            List<String> texts = chunks.stream().map(ChunkResult::getContent).toList();
            List<float[]> embeddings = List.of();
            if (embeddingService.isAvailable()) {
                embeddings = embeddingService.embedBatch(texts);
                if (embeddings.size() != texts.size()) {
                    throw new BizException("向量嵌入数量与分块数量不一致（"
                            + embeddings.size() + "/" + texts.size() + "）");
                }
            }

            LocalDateTime now = LocalDateTime.now();
            List<AiKnowledgeChunk> chunkEntities = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                ChunkResult chunk = chunks.get(i);
                AiKnowledgeChunk entity = new AiKnowledgeChunk();
                entity.setDocId(docId);
                entity.setKbId(doc.getKbId());
                entity.setChunkIndex(chunk.getChunkIndex());
                entity.setContent(chunk.getContent());
                entity.setPageNum(chunk.getPageNum());
                entity.setSectionTitle(chunk.getSectionTitle());
                entity.setDocVersion(newVersion);
                entity.setCreateTime(now);
                if (embeddingService.isAvailable()) {
                    entity.setEmbeddingJson(VectorUtils.serialize(embeddings.get(i), objectMapper));
                }
                chunkEntities.add(entity);
            }

            if (!chunkEntities.isEmpty()) {
                aiKnowledgeChunkMapper.insertBatch(chunkEntities);
            }

            doc.setContent(parseResult.getFullText());
            doc.setChunkCount(chunks.size());
            doc.setDocVersion(newVersion);
            doc.setIndexStatus(AiIndexServiceImpl.INDEX_DONE);
            doc.setErrorMsg(null);
            doc.setIndexedAt(now);
            doc.setUpdateTime(now);
            aiKnowledgeDocumentMapper.updateById(doc);

            aiKnowledgeChunkMapper.delete(new LambdaQueryWrapper<AiKnowledgeChunk>()
                    .eq(AiKnowledgeChunk::getDocId, docId)
                    .lt(AiKnowledgeChunk::getDocVersion, newVersion));

            updateTaskStatus(taskId, AiIndexServiceImpl.STATUS_DONE);
            log.info("[IndexService] 索引完成 docId={} version={} chunks={}", docId, newVersion, chunks.size());
        } catch (Exception ex) {
            log.error("[IndexService] 索引失败 docId={}", docId, ex);
            markFailed(taskId, docId, ex.getMessage() != null ? ex.getMessage() : "索引失败");
        }
    }

    private ParseResult loadParseResult(AiKnowledgeDocument doc) throws Exception {
        if (StringUtils.hasText(doc.getObjectName())) {
            FileStorageService storage = fileStorageServiceProvider.getIfAvailable();
            if (storage != null) {
                byte[] bytes = storage.downloadObject(doc.getObjectName());
                String fileName = StringUtils.hasText(doc.getOriginalFileName())
                        ? doc.getOriginalFileName()
                        : doc.getTitle();
                return documentLoaderService.load(new ByteArrayInputStream(bytes), fileName);
            }
        }
        if (StringUtils.hasText(doc.getContent())) {
            return ParseResult.builder()
                    .success(true)
                    .pages(List.of(ParseResult.PageContent.builder()
                            .pageNum(1)
                            .text(doc.getContent())
                            .build()))
                    .totalPages(1)
                    .build();
        }
        return ParseResult.failure("文档无可用内容");
    }

    private void markFailed(Long taskId, Long docId, String errorMsg) {
        AiIndexTask task = aiIndexTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(AiIndexServiceImpl.STATUS_FAILED);
            task.setErrorMsg(errorMsg);
            task.setFinishedAt(LocalDateTime.now());
            aiIndexTaskMapper.updateById(task);
        }
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc != null) {
            doc.setIndexStatus(AiIndexServiceImpl.INDEX_FAILED);
            doc.setErrorMsg(errorMsg);
            doc.setUpdateTime(LocalDateTime.now());
            aiKnowledgeDocumentMapper.updateById(doc);
        }
    }

    private void updateTaskStatus(Long taskId, String status) {
        AiIndexTask task = aiIndexTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(status);
        if (AiIndexServiceImpl.STATUS_RUNNING.equals(status)) {
            task.setStartedAt(LocalDateTime.now());
        }
        if (AiIndexServiceImpl.STATUS_DONE.equals(status) || AiIndexServiceImpl.STATUS_FAILED.equals(status)) {
            task.setFinishedAt(LocalDateTime.now());
        }
        aiIndexTaskMapper.updateById(task);
    }

    private void updateDocIndexStatus(Long docId, String status, String errorMsg) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc == null) {
            return;
        }
        doc.setIndexStatus(status);
        doc.setErrorMsg(errorMsg);
        doc.setUpdateTime(LocalDateTime.now());
        aiKnowledgeDocumentMapper.updateById(doc);
    }
}
