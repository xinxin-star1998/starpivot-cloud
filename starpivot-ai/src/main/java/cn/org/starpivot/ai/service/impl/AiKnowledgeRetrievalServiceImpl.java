package cn.org.starpivot.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.rag.*;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
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

        boolean rerankerOn = ragRerankerService.isEnabled();
        long rerankStart = System.currentTimeMillis();
        hits = ragRerankerService.rerank(normalizedQuery, hits, limit);
        hits = hits.stream().filter(hit -> StringUtils.hasText(hit.getContent())).toList();
        aiMetrics.recordRagStage("rerank", System.currentTimeMillis() - rerankStart);

        if (RagConfidenceFilter.shouldDrop(hits, rag.getMinVectorScore(), rerankerOn)) {
            log.info("[RAG] drop weak hits maxScore below {} reranked={}", rag.getMinVectorScore(), rerankerOn);
            recordRagMetrics(totalStart, 0);
            return RagRetrievalResult.builder().context("").sources(List.of()).degraded(true).build();
        }

        hits = diversifyByDoc(hits, limit, 2);
        hits = expandWithNeighbors(hits);

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
            if (StringUtils.hasText(hit.getSectionTitle())) {
                builder.append(" / ").append(hit.getSectionTitle().trim());
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

    /**
     * 控制单文档占比：先按文档轮询取满额度，再回填剩余名额，避免 topK 被同一文档占满。
     */
    static List<AiKnowledgeChunkHitVo> diversifyByDoc(List<AiKnowledgeChunkHitVo> hits, int limit, int maxPerDoc) {
        if (hits == null || hits.isEmpty() || limit <= 0) {
            return List.of();
        }
        int perDoc = Math.max(maxPerDoc, 1);
        List<AiKnowledgeChunkHitVo> selected = new ArrayList<>();
        Map<Long, Integer> docCounts = new HashMap<>();
        List<AiKnowledgeChunkHitVo> overflow = new ArrayList<>();

        for (AiKnowledgeChunkHitVo hit : hits) {
            if (selected.size() >= limit) {
                break;
            }
            Long docId = hit.getDocId() != null ? hit.getDocId() : -1L;
            int count = docCounts.getOrDefault(docId, 0);
            if (count < perDoc) {
                selected.add(hit);
                docCounts.put(docId, count + 1);
            } else {
                overflow.add(hit);
            }
        }
        for (AiKnowledgeChunkHitVo hit : overflow) {
            if (selected.size() >= limit) {
                break;
            }
            selected.add(hit);
        }
        return selected;
    }

    /**
     * 命中块前后各扩 1 块，补齐被滑动窗口切断的步骤/段落。
     */
    private List<AiKnowledgeChunkHitVo> expandWithNeighbors(List<AiKnowledgeChunkHitVo> hits) {
        if (hits == null || hits.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<Integer>> neededIndexes = new LinkedHashMap<>();
        for (AiKnowledgeChunkHitVo hit : hits) {
            if (hit.getDocId() == null || hit.getChunkIndex() == null) {
                continue;
            }
            Set<Integer> indexes = neededIndexes.computeIfAbsent(hit.getDocId(), key -> new LinkedHashSet<>());
            if (hit.getChunkIndex() > 0) {
                indexes.add(hit.getChunkIndex() - 1);
            }
            indexes.add(hit.getChunkIndex());
            indexes.add(hit.getChunkIndex() + 1);
        }

        Map<Long, Map<Integer, AiKnowledgeChunkHitVo>> byDoc = new HashMap<>();
        for (Map.Entry<Long, Set<Integer>> entry : neededIndexes.entrySet()) {
            try {
                List<AiKnowledgeChunkHitVo> neighbors = aiKnowledgeChunkMapper.listByDocAndChunkIndexes(
                        entry.getKey(), new ArrayList<>(entry.getValue()));
                Map<Integer, AiKnowledgeChunkHitVo> indexMap = new HashMap<>();
                for (AiKnowledgeChunkHitVo neighbor : neighbors) {
                    if (neighbor.getChunkIndex() != null) {
                        indexMap.put(neighbor.getChunkIndex(), neighbor);
                    }
                }
                byDoc.put(entry.getKey(), indexMap);
            } catch (Exception ex) {
                log.debug("[RAG] neighbor expand failed docId={}: {}", entry.getKey(), ex.getMessage());
            }
        }

        List<AiKnowledgeChunkHitVo> expanded = new ArrayList<>(hits.size());
        for (AiKnowledgeChunkHitVo hit : hits) {
            if (hit.getDocId() == null || hit.getChunkIndex() == null) {
                expanded.add(hit);
                continue;
            }
            Map<Integer, AiKnowledgeChunkHitVo> indexMap = byDoc.get(hit.getDocId());
            if (indexMap == null || indexMap.isEmpty()) {
                expanded.add(hit);
                continue;
            }
            String merged = mergeNeighborContent(hit, indexMap);
            if (!StringUtils.hasText(merged) || merged.equals(hit.getContent())) {
                expanded.add(hit);
                continue;
            }
            AiKnowledgeChunkHitVo copy = copyHit(hit);
            copy.setContent(merged);
            expanded.add(copy);
        }
        return expanded;
    }

    static String mergeNeighborContent(AiKnowledgeChunkHitVo hit, Map<Integer, AiKnowledgeChunkHitVo> indexMap) {
        StringBuilder builder = new StringBuilder();
        AiKnowledgeChunkHitVo prev = indexMap.get(hit.getChunkIndex() - 1);
        if (prev != null && StringUtils.hasText(prev.getContent())) {
            builder.append(prev.getContent().trim()).append("\n");
        }
        String self = StringUtils.hasText(hit.getContent()) ? hit.getContent().trim() : "";
        if (StringUtils.hasText(self)) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append(self);
        }
        AiKnowledgeChunkHitVo next = indexMap.get(hit.getChunkIndex() + 1);
        if (next != null && StringUtils.hasText(next.getContent())) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append(next.getContent().trim());
        }
        return builder.toString();
    }

    private static AiKnowledgeChunkHitVo copyHit(AiKnowledgeChunkHitVo source) {
        AiKnowledgeChunkHitVo copy = new AiKnowledgeChunkHitVo();
        copy.setChunkId(source.getChunkId());
        copy.setDocId(source.getDocId());
        copy.setKbId(source.getKbId());
        copy.setDocTitle(source.getDocTitle());
        copy.setContent(source.getContent());
        copy.setScore(source.getScore());
        copy.setPageNum(source.getPageNum());
        copy.setSectionTitle(source.getSectionTitle());
        copy.setChunkIndex(source.getChunkIndex());
        copy.setEmbeddingJson(source.getEmbeddingJson());
        return copy;
    }

    private List<AiKnowledgeChunkHitVo> retrieveCandidates(String query, int candidateTopK) {
        List<String> queries = RagQueryExpandUtils.expand(query);
        List<AiKnowledgeChunkHitVo> primary = hybridRetrieve(queries.get(0), candidateTopK, true);
        if (queries.size() == 1) {
            return primary;
        }
        // 并列子查询只走全文，避免再扫一遍全库向量
        List<List<AiKnowledgeChunkHitVo>> rankedLists = new ArrayList<>();
        rankedLists.add(primary);
        AiProperties.RagProperties rag = aiProperties.getRag();
        int fulltextK = Math.max(candidateTopK, rag.getFulltextTopK());
        for (int i = 1; i < queries.size(); i++) {
            rankedLists.add(deduplicate(searchFulltext(queries.get(i), fulltextK), candidateTopK));
        }
        log.info("[RAG] compound query expanded={} merged from {} lists", queries.size(), rankedLists.size());
        return RagRrfMergeUtils.mergeLists(rankedLists, candidateTopK);
    }

    private List<AiKnowledgeChunkHitVo> hybridRetrieve(String query, int candidateTopK, boolean enableHyde) {
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

        if (enableHyde
                && ragQueryRewriterService.isEnabled()
                && embeddingService.isAvailable()
                && !merged.isEmpty()
                && !shouldSkipHyde(vectorHits, rag.getHydeSkipMinScore())) {
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

    private static boolean shouldSkipHyde(List<AiKnowledgeChunkHitVo> vectorHits, double skipMinScore) {
        if (skipMinScore <= 0 || vectorHits == null || vectorHits.isEmpty()) {
            return false;
        }
        double max = vectorHits.stream()
                .map(AiKnowledgeChunkHitVo::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);
        return max >= skipMinScore;
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
