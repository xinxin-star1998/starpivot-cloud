package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RagRrfMergeUtils {

    private static final int RRF_K = 60;

    private RagRrfMergeUtils() {}

    public static List<AiKnowledgeChunkHitVo> mergeLists(List<List<AiKnowledgeChunkHitVo>> rankedLists, int topK) {
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

    public static List<AiKnowledgeChunkHitVo> merge(
            List<AiKnowledgeChunkHitVo> vectorList,
            List<AiKnowledgeChunkHitVo> fulltextList,
            int topK) {
        return mergeLists(List.of(vectorList, fulltextList), topK);
    }
}
