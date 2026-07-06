package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.rag.EmbeddingService;
import cn.org.starpivot.ai.rag.RagQueryRewriterService;
import cn.org.starpivot.ai.rag.RagRerankerService;
import cn.org.starpivot.ai.rag.VectorUtils;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeRetrievalServiceImpl implements AiKnowledgeRetrievalService {

    private static final int RRF_K = 60;

    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final EmbeddingService embeddingService;
    private final RagQueryRewriterService ragQueryRewriterService;
    private final RagRerankerService ragRerankerService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public RagRetrievalResult retrieve(String query, int topK) {
        if (!StringUtils.hasText(query) || topK <= 0) {
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }

        String normalizedQuery = query.trim();
        int configuredMax = Math.max(rag.getRetrieveTopK(), 1);
        int limit = Math.min(Math.max(topK, 1), configuredMax);
        AiProperties.RagProperties rag = aiProperties.getRag();
        int candidateTopK = Math.max(limit, rag.getRetrieveTopK());

        List<AiKnowledgeChunkHitVo> hits = retrieveCandidates(normalizedQuery, candidateTopK);
        if (hits.isEmpty()) {
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }

        hits = ragRerankerService.rerank(normalizedQuery, hits, limit);

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

        return RagRetrievalResult.builder()
                .context(builder.toString().trim())
                .sources(sources)
                .build();
    }

    private List<AiKnowledgeChunkHitVo> retrieveCandidates(String query, int candidateTopK) {
        AiProperties.RagProperties rag = aiProperties.getRag();
        int vectorK = Math.max(candidateTopK, rag.getVectorTopK());
        int fulltextK = Math.max(candidateTopK, rag.getFulltextTopK());

        List<AiKnowledgeChunkHitVo> fulltextHits = searchFulltext(query, fulltextK);
        List<AiKnowledgeChunkHitVo> vectorHits = searchVector(query, vectorK);

        List<AiKnowledgeChunkHitVo> merged;
        if (vectorHits.isEmpty()) {
            merged = deduplicate(fulltextHits, candidateTopK);
        } else if (fulltextHits.isEmpty()) {
            merged = deduplicate(vectorHits, candidateTopK);
        } else {
            merged = rrfMerge(vectorHits, fulltextHits, candidateTopK);
        }

        if (ragQueryRewriterService.isEnabled() && embeddingService.isAvailable() && !merged.isEmpty()) {
            String hydeText = ragQueryRewriterService.generateHypotheticalAnswer(query);
            List<AiKnowledgeChunkHitVo> hydeHits = searchVector(hydeText, vectorK);
            if (!hydeHits.isEmpty()) {
                merged = rrfMergeLists(List.of(merged, hydeHits), candidateTopK);
                log.debug("[RAG] HyDE merged, candidates={}", merged.size());
            }
        }

        return merged;
    }

    private List<AiKnowledgeChunkHitVo> searchFulltext(String query, int topK) {
        List<AiKnowledgeChunkHitVo> hits = new ArrayList<>();
        try {
            hits.addAll(aiKnowledgeChunkMapper.searchFulltext(query, topK));
        } catch (Exception ex) {
            log.debug("FULLTEXT search unavailable, fallback to LIKE: {}", ex.getMessage());
        }
        if (!hits.isEmpty()) {
            return hits;
        }
        for (String keyword : extractKeywords(query)) {
            hits.addAll(aiKnowledgeChunkMapper.searchLike(keyword, topK));
            if (hits.size() >= topK) {
                break;
            }
        }
        return hits;
    }

    private List<AiKnowledgeChunkHitVo> searchVector(String query, int topK) {
        if (!embeddingService.isAvailable()) {
            return List.of();
        }
        float[] queryVector;
        try {
            queryVector = embeddingService.embed(query);
        } catch (RuntimeException ex) {
            log.warn("Vector embedding failed, skip vector search: {}", ex.getMessage());
            return List.of();
        }
        if (queryVector.length == 0) {
            return List.of();
        }

        double minScore = aiProperties.getRag().getMinVectorScore();
        List<AiKnowledgeChunkHitVo> candidates = aiKnowledgeChunkMapper.listEmbeddableChunks(5000);
        if (candidates.isEmpty()) {
            return List.of();
        }
        for (AiKnowledgeChunkHitVo candidate : candidates) {
            float[] vector = VectorUtils.deserialize(candidate.getEmbeddingJson(), objectMapper);
            candidate.setScore(VectorUtils.cosineSimilarity(queryVector, vector));
        }
        return candidates.stream()
                .filter(hit -> hit.getScore() != null && hit.getScore() > minScore)
                .sorted(Comparator.comparing(AiKnowledgeChunkHitVo::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    private List<AiKnowledgeChunkHitVo> rrfMerge(
            List<AiKnowledgeChunkHitVo> vectorList,
            List<AiKnowledgeChunkHitVo> fulltextList,
            int topK) {
        return rrfMergeLists(List.of(vectorList, fulltextList), topK);
    }

    private List<AiKnowledgeChunkHitVo> rrfMergeLists(List<List<AiKnowledgeChunkHitVo>> rankedLists, int topK) {
        Map<Long, Double> scoreMap = new LinkedHashMap<>();
        Map<Long, AiKnowledgeChunkHitVo> chunkMap = new LinkedHashMap<>();

        for (List<AiKnowledgeChunkHitVo> list : rankedLists) {
            for (int rank = 0; rank < list.size(); rank++) {
                AiKnowledgeChunkHitVo chunk = list.get(rank);
                if (chunk.getChunkId() == null) {
                    continue;
                }
                scoreMap.merge(chunk.getChunkId(), 1.0D / (RRF_K + rank + 1), Double::sum);
                chunkMap.putIfAbsent(chunk.getChunkId(), chunk);
            }
        }

        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> {
                    AiKnowledgeChunkHitVo hit = chunkMap.get(entry.getKey());
                    hit.setScore(entry.getValue());
                    return hit;
                })
                .toList();
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
