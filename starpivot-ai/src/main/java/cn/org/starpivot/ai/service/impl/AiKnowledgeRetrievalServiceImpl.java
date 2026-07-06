package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.rag.*;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiKnowledgeRetrievalServiceImpl implements AiKnowledgeRetrievalService {

    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final EmbeddingService embeddingService;
    private final RagQueryRewriterService ragQueryRewriterService;
    private final RagRerankerService ragRerankerService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final Executor ragRetrievalExecutor;
    private final AiMetrics aiMetrics;

    public AiKnowledgeRetrievalServiceImpl(
            AiKnowledgeChunkMapper aiKnowledgeChunkMapper,
            EmbeddingService embeddingService,
            RagQueryRewriterService ragQueryRewriterService,
            RagRerankerService ragRerankerService,
            AiProperties aiProperties,
            ObjectMapper objectMapper,
            @Qualifier("ragRetrievalExecutor") Executor ragRetrievalExecutor,
            AiMetrics aiMetrics) {
        this.aiKnowledgeChunkMapper = aiKnowledgeChunkMapper;
        this.embeddingService = embeddingService;
        this.ragQueryRewriterService = ragQueryRewriterService;
        this.ragRerankerService = ragRerankerService;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.ragRetrievalExecutor = ragRetrievalExecutor;
        this.aiMetrics = aiMetrics;
    }

    @Override
    public RagRetrievalResult retrieve(String query, int topK) {
        if (!StringUtils.hasText(query) || topK <= 0) {
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }

        long totalStart = System.currentTimeMillis();
        String normalizedQuery = query.trim();
        AiProperties.RagProperties rag = aiProperties.getRag();
        int configuredMax = Math.max(rag.getRetrieveTopK(), 1);
        int limit = Math.min(Math.max(topK, 1), configuredMax);
        int candidateTopK = Math.max(limit, rag.getRetrieveTopK());

        List<AiKnowledgeChunkHitVo> hits = retrieveCandidates(normalizedQuery, candidateTopK);
        if (hits.isEmpty()) {
            recordRagMetrics(totalStart, 0);
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }

        long rerankStart = System.currentTimeMillis();
        hits = ragRerankerService.rerank(normalizedQuery, hits, limit);
        hits = hits.stream().filter(hit -> StringUtils.hasText(hit.getContent())).toList();
        aiMetrics.recordRagStage("rerank", System.currentTimeMillis() - rerankStart);

        List<RagSourceVo> sources = hits.stream()
                .map(hit -> RagSourceVo.builder()
                        .chunkId(hit.getChunkId())
                        .docId(hit.getDocId())
                        .docTitle(hit.getDocTitle())
                        .snippet(truncate(hit.getContent(), 180))
                        .score(hit.getScore())
                        .pageNum(hit.getPageNum())
                        .build())
                .toList();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            AiKnowledgeChunkHitVo hit = hits.get(i);
            builder.append("【资料").append(i + 1);
            if (StringUtils.hasText(hit.getDocTitle())) {
                builder.append(" - ").append(hit.getDocTitle().trim());
            }
            if (hit.getPageNum() != null && hit.getPageNum() > 0) {
                builder.append(" 第").append(hit.getPageNum()).append("页");
            }
            builder.append("】\n");
            builder.append(hit.getContent().trim()).append("\n\n");
        }

        recordRagMetrics(totalStart, hits.size());
        log.info("[RAG] retrieve complete queryLen={} hits={} durationMs={}",
                normalizedQuery.length(), hits.size(), System.currentTimeMillis() - totalStart);

        return RagRetrievalResult.builder()
                .context(builder.toString().trim())
                .sources(sources)
                .build();
    }

    private List<AiKnowledgeChunkHitVo> retrieveCandidates(String query, int candidateTopK) {
        AiProperties.RagProperties rag = aiProperties.getRag();
        int vectorK = Math.max(candidateTopK, rag.getVectorTopK());
        int fulltextK = Math.max(candidateTopK, rag.getFulltextTopK());

        long parallelStart = System.currentTimeMillis();
        CompletableFuture<List<AiKnowledgeChunkHitVo>> fulltextFuture = CompletableFuture.supplyAsync(
                () -> searchFulltext(query, fulltextK), ragRetrievalExecutor);
        CompletableFuture<List<AiKnowledgeChunkHitVo>> vectorFuture = CompletableFuture.supplyAsync(
                () -> searchVector(query, vectorK), ragRetrievalExecutor);

        List<AiKnowledgeChunkHitVo> fulltextHits = fulltextFuture.join();
        List<AiKnowledgeChunkHitVo> vectorHits = vectorFuture.join();
        long parallelMs = System.currentTimeMillis() - parallelStart;
        aiMetrics.recordRagStage("parallel_retrieve", parallelMs);
        log.info("[RAG] stage=parallel_retrieve fulltextHits={} vectorHits={} durationMs={}",
                fulltextHits.size(), vectorHits.size(), parallelMs);

        List<AiKnowledgeChunkHitVo> merged;
        if (vectorHits.isEmpty()) {
            merged = deduplicate(fulltextHits, candidateTopK);
        } else if (fulltextHits.isEmpty()) {
            merged = deduplicate(vectorHits, candidateTopK);
        } else {
            merged = RagRrfMergeUtils.merge(vectorHits, fulltextHits, candidateTopK);
        }

        if (ragQueryRewriterService.isEnabled() && embeddingService.isAvailable() && !merged.isEmpty()) {
            long hydeStart = System.currentTimeMillis();
            String hydeText = ragQueryRewriterService.generateHypotheticalAnswer(query);
            if (StringUtils.hasText(hydeText)) {
                List<AiKnowledgeChunkHitVo> hydeHits = searchVector(hydeText, vectorK);
                if (!hydeHits.isEmpty()) {
                    merged = RagRrfMergeUtils.mergeLists(List.of(merged, hydeHits), candidateTopK);
                }
            }
            aiMetrics.recordRagStage("hyde", System.currentTimeMillis() - hydeStart);
            log.info("[RAG] stage=hyde mergedHits={} durationMs={}",
                    merged.size(), System.currentTimeMillis() - hydeStart);
        }

        return merged;
    }

    private List<AiKnowledgeChunkHitVo> searchFulltext(String query, int topK) {
        long start = System.currentTimeMillis();
        List<AiKnowledgeChunkHitVo> hits = new ArrayList<>();
        try {
            hits.addAll(aiKnowledgeChunkMapper.searchFulltext(query, topK));
        } catch (Exception ex) {
            log.debug("[RAG] FULLTEXT unavailable, fallback to LIKE: {}", ex.getMessage());
        }
        if (hits.isEmpty()) {
            for (String keyword : extractKeywords(query)) {
                hits.addAll(aiKnowledgeChunkMapper.searchLike(keyword, topK));
                if (hits.size() >= topK) {
                    break;
                }
            }
        }
        aiMetrics.recordRagStage("fulltext", System.currentTimeMillis() - start);
        return hits;
    }

    private List<AiKnowledgeChunkHitVo> searchVector(String query, int topK) {
        long start = System.currentTimeMillis();
        if (!embeddingService.isAvailable()) {
            return List.of();
        }
        float[] queryVector;
        try {
            queryVector = embeddingService.embed(query);
        } catch (RuntimeException ex) {
            log.warn("[RAG] vector embedding failed: {}", ex.getMessage());
            return List.of();
        }
        if (queryVector.length == 0) {
            return List.of();
        }

        double minScore = aiProperties.getRag().getMinVectorScore();
        int batchSize = Math.max(aiProperties.getRag().getVectorScanBatchSize(), 500);
        int maxScan = aiProperties.getRag().getVectorScanMaxChunks();
        int poolSize = Math.max(topK * 3, topK);
        PriorityQueue<AiKnowledgeChunkHitVo> topCandidates = new PriorityQueue<>(
                poolSize, Comparator.comparingDouble(hit -> hit.getScore() != null ? hit.getScore() : 0D));

        Long lastChunkId = null;
        int scanned = 0;
        while (true) {
            List<AiKnowledgeChunkHitVo> batch = aiKnowledgeChunkMapper.listEmbeddableChunkBatch(lastChunkId, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            for (AiKnowledgeChunkHitVo candidate : batch) {
                float[] vector = VectorUtils.deserialize(candidate.getEmbeddingJson(), objectMapper);
                double score = VectorUtils.cosineSimilarity(queryVector, vector);
                candidate.setScore(score);
                if (score <= minScore) {
                    continue;
                }
                if (topCandidates.size() < poolSize) {
                    topCandidates.offer(candidate);
                } else if (score > topCandidates.peek().getScore()) {
                    topCandidates.poll();
                    topCandidates.offer(candidate);
                }
            }
            lastChunkId = batch.get(batch.size() - 1).getChunkId();
            scanned += batch.size();
            if (maxScan > 0 && scanned >= maxScan) {
                log.warn("[RAG] vector scan reached maxScan={} chunks, results may be incomplete", maxScan);
                break;
            }
            if (batch.size() < batchSize) {
                break;
            }
        }

        List<AiKnowledgeChunkHitVo> hits = topCandidates.stream()
                .sorted(Comparator.comparing(AiKnowledgeChunkHitVo::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
        aiMetrics.recordRagStage("vector", System.currentTimeMillis() - start);
        log.debug("[RAG] vector search scanned={} hits={}", scanned, hits.size());
        return hits;
    }

    private void recordRagMetrics(long startMs, int hitCount) {
        aiMetrics.recordRag(System.currentTimeMillis() - startMs, hitCount);
    }

    private List<AiKnowledgeChunkHitVo> deduplicate(List<AiKnowledgeChunkHitVo> hits, int topK) {
        Map<Long, AiKnowledgeChunkHitVo> unique = new LinkedHashMap<>();
        for (AiKnowledgeChunkHitVo hit : hits) {
            if (hit.getChunkId() != null) {
                unique.putIfAbsent(hit.getChunkId(), hit);
            }
        }
        return unique.values().stream().limit(topK).toList();
    }

    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        String[] parts = query.split("[\\s,，。！？；;]+");
        for (String part : parts) {
            if (StringUtils.hasText(part) && part.trim().length() >= 2) {
                keywords.add(part.trim());
            }
        }
        if (keywords.isEmpty() && query.length() >= 2) {
            keywords.add(query.length() > 24 ? query.substring(0, 24) : query);
        }
        return keywords.stream().limit(5).toList();
    }

    private String truncate(String text, int maxLen) {
        if (!StringUtils.hasText(text) || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }
}
