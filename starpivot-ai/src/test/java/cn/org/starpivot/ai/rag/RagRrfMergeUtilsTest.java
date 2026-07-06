package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagRrfMergeUtilsTest {

    @Test
    void mergeLists_combinesRankingsWithRrf() {
        AiKnowledgeChunkHitVo a = hit(1L, "A");
        AiKnowledgeChunkHitVo b = hit(2L, "B");
        AiKnowledgeChunkHitVo c = hit(3L, "C");

        List<AiKnowledgeChunkHitVo> merged = RagRrfMergeUtils.mergeLists(
                List.of(List.of(a, b), List.of(c, a)),
                2);

        assertEquals(2, merged.size());
        assertEquals(1L, merged.get(0).getChunkId());
        assertTrue(merged.get(0).getScore() > 0);
    }

    @Test
    void merge_deduplicatesAcrossSources() {
        AiKnowledgeChunkHitVo shared = hit(10L, "shared");
        AiKnowledgeChunkHitVo other = hit(20L, "other");

        List<AiKnowledgeChunkHitVo> merged = RagRrfMergeUtils.merge(
                List.of(shared),
                List.of(shared, other),
                2);

        assertEquals(2, merged.size());
        assertEquals(10L, merged.get(0).getChunkId());
    }

    private AiKnowledgeChunkHitVo hit(Long chunkId, String content) {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setChunkId(chunkId);
        hit.setContent(content);
        return hit;
    }
}
