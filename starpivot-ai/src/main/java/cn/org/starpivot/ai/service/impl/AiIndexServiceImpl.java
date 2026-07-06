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
import cn.org.starpivot.ai.service.AiIndexService;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.FileStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiIndexServiceImpl implements AiIndexService {

    public static final String TASK_TYPE_TEXT = "INDEX_TEXT";
    public static final String TASK_TYPE_FILE = "INDEX_FILE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String INDEX_PENDING = "PENDING";
    public static final String INDEX_PROCESSING = "PROCESSING";
    public static final String INDEX_DONE = "DONE";
    public static final String INDEX_FAILED = "FAILED";

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;
    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final AiIndexTaskMapper aiIndexTaskMapper;
    private final RagChunkService ragChunkService;
    private final EmbeddingService embeddingService;
    private final DocumentLoaderService documentLoaderService;
    private final ObjectProvider<FileStorageService> fileStorageServiceProvider;
    private final ObjectMapper objectMapper;
    private final AiIndexServiceImpl self;

    public AiIndexServiceImpl(AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper,
                              AiKnowledgeBaseMapper aiKnowledgeBaseMapper,
                              AiKnowledgeChunkMapper aiKnowledgeChunkMapper,
                              AiIndexTaskMapper aiIndexTaskMapper,
                              RagChunkService ragChunkService,
                              EmbeddingService embeddingService,
                              DocumentLoaderService documentLoaderService,
                              ObjectProvider<FileStorageService> fileStorageServiceProvider,
                              ObjectMapper objectMapper,
                              @Lazy AiIndexServiceImpl self) {
        this.aiKnowledgeDocumentMapper = aiKnowledgeDocumentMapper;
        this.aiKnowledgeBaseMapper = aiKnowledgeBaseMapper;
        this.aiKnowledgeChunkMapper = aiKnowledgeChunkMapper;
        this.aiIndexTaskMapper = aiIndexTaskMapper;
        this.ragChunkService = ragChunkService;
        this.embeddingService = embeddingService;
        this.documentLoaderService = documentLoaderService;
        this.fileStorageServiceProvider = fileStorageServiceProvider;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTextIndex(Long docId) {
        assertNoActiveIndexTask(docId);
        createTask(docId, TASK_TYPE_TEXT);
        self.executeAsync(requireDocument(docId).getDocId(), latestTaskId(docId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFileIndex(Long docId) {
        assertNoActiveIndexTask(docId);
        createTask(docId, TASK_TYPE_FILE);
        self.executeAsync(docId, latestTaskId(docId));
    }

    @Override
    public void executeTask(Long taskId, Long docId) {
        self.executeAsync(docId, taskId);
    }

    @Override
    public AiKnowledgeDocument requireReadyDocument(Long docId) {
        AiKnowledgeDocument doc = requireDocument(docId);
        if (INDEX_PROCESSING.equals(doc.getIndexStatus())) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
        return doc;
    }

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

        updateTaskStatus(taskId, STATUS_RUNNING);
        updateDocIndexStatus(docId, INDEX_PROCESSING, null);

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
            List<float[]> embeddings = embeddingService.isAvailable() ? embeddingService.embedBatch(texts) : List.of();

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
                if (embeddingService.isAvailable() && i < embeddings.size()) {
                    entity.setEmbeddingJson(VectorUtils.serialize(embeddings.get(i), objectMapper));
                }
                chunkEntities.add(entity);
            }

            for (AiKnowledgeChunk chunkEntity : chunkEntities) {
                aiKnowledgeChunkMapper.insert(chunkEntity);
            }

            aiKnowledgeChunkMapper.delete(new LambdaQueryWrapper<AiKnowledgeChunk>()
                    .eq(AiKnowledgeChunk::getDocId, docId)
                    .lt(AiKnowledgeChunk::getDocVersion, newVersion));

            doc.setContent(parseResult.getFullText());
            doc.setChunkCount(chunks.size());
            doc.setDocVersion(newVersion);
            doc.setIndexStatus(INDEX_DONE);
            doc.setErrorMsg(null);
            doc.setIndexedAt(now);
            doc.setUpdateTime(now);
            aiKnowledgeDocumentMapper.updateById(doc);

            updateTaskStatus(taskId, STATUS_DONE);
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

    private void assertNoActiveIndexTask(Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc != null && (INDEX_PROCESSING.equals(doc.getIndexStatus()) || INDEX_PENDING.equals(doc.getIndexStatus()))) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
        Long activeCount = aiIndexTaskMapper.selectCount(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .in(AiIndexTask::getStatus, STATUS_PENDING, STATUS_RUNNING));
        if (activeCount != null && activeCount > 0) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
    }

    private void createTask(Long docId, String taskType) {
        AiIndexTask task = new AiIndexTask();
        task.setDocId(docId);
        task.setTaskType(taskType);
        task.setStatus(STATUS_PENDING);
        task.setRetryCount(0);
        task.setCreateTime(LocalDateTime.now());
        aiIndexTaskMapper.insert(task);

        AiKnowledgeDocument doc = requireDocument(docId);
        doc.setIndexStatus(INDEX_PENDING);
        doc.setErrorMsg(null);
        doc.setUpdateTime(LocalDateTime.now());
        aiKnowledgeDocumentMapper.updateById(doc);
    }

    private Long latestTaskId(Long docId) {
        AiIndexTask task = aiIndexTaskMapper.selectOne(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .orderByDesc(AiIndexTask::getTaskId)
                .last("LIMIT 1"));
        if (task == null) {
            throw new BizException("索引任务不存在");
        }
        return task.getTaskId();
    }

    private AiKnowledgeDocument requireDocument(Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        return doc;
    }

    private void markFailed(Long taskId, Long docId, String errorMsg) {
        AiIndexTask task = aiIndexTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(STATUS_FAILED);
            task.setErrorMsg(errorMsg);
            task.setFinishedAt(LocalDateTime.now());
            aiIndexTaskMapper.updateById(task);
        }
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc != null) {
            doc.setIndexStatus(INDEX_FAILED);
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
        if (STATUS_RUNNING.equals(status)) {
            task.setStartedAt(LocalDateTime.now());
        }
        if (STATUS_DONE.equals(status) || STATUS_FAILED.equals(status)) {
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
