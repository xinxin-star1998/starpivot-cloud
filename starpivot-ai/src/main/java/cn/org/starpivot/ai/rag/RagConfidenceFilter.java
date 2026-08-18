package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;

import java.util.List;
import java.util.Objects;

/**
 * 精排/向量分过低时丢掉噪声命中。
 * <ul>
 *   <li>语义分（通常 ≥ 0.15）：低于 minScore 则丢弃</li>
 *   <li>RRF 分（通常 &lt; 0.15）：无重排时若最高分仍极低（&lt; 0.02）则丢弃</li>
 * </ul>
 */
public final class RagConfidenceFilter {

    static final double SEMANTIC_SCORE_HINT = 0.15D;
    /** 典型 RRF top1 ≈ 1/(60+1)≈0.016；过低说明各路几乎没命中 */
    static final double WEAK_RRF_SCORE = 0.02D;

    private RagConfidenceFilter() {}

    public static boolean shouldDrop(List<AiKnowledgeChunkHitVo> hits, double minScore) {
        return shouldDrop(hits, minScore, false);
    }

    public static boolean shouldDrop(List<AiKnowledgeChunkHitVo> hits, double minScore, boolean reranked) {
        if (hits == null || hits.isEmpty()) {
            return true;
        }
        double max = hits.stream()
                .map(AiKnowledgeChunkHitVo::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);
        if (Double.isNaN(max)) {
            return false;
        }
        if (max >= SEMANTIC_SCORE_HINT || reranked) {
            return max < minScore;
        }
        return max < WEAK_RRF_SCORE;
    }
}
