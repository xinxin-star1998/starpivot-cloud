package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagConfidenceFilterTest {

    @Test
    void shouldDrop_keepsHealthyRrfScores() {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setScore(0.032);
        assertFalse(RagConfidenceFilter.shouldDrop(List.of(hit), 0.3));
    }

    @Test
    void shouldDrop_filtersExtremelyWeakRrfScores() {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setScore(0.01);
        assertTrue(RagConfidenceFilter.shouldDrop(List.of(hit), 0.3, false));
    }

    @Test
    void shouldDrop_filtersWeakSemanticScores() {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setScore(0.18);
        assertTrue(RagConfidenceFilter.shouldDrop(List.of(hit), 0.3));
    }

    @Test
    void shouldDrop_keepsStrongSemanticScores() {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setScore(0.72);
        assertFalse(RagConfidenceFilter.shouldDrop(List.of(hit), 0.3));
    }
}
