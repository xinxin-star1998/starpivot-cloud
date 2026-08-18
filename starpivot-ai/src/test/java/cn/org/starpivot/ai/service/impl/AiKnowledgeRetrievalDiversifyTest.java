package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiKnowledgeRetrievalDiversifyTest {

    @Test
    void diversifyByDoc_limitsSameDocument() {
        List<AiKnowledgeChunkHitVo> hits = List.of(
                hit(1L, 10L),
                hit(2L, 10L),
                hit(3L, 10L),
                hit(4L, 20L),
                hit(5L, 30L));

        List<AiKnowledgeChunkHitVo> selected = AiKnowledgeRetrievalServiceImpl.diversifyByDoc(hits, 4, 2);

        assertEquals(4, selected.size());
        assertEquals(1L, selected.get(0).getChunkId());
        assertEquals(2L, selected.get(1).getChunkId());
        assertEquals(4L, selected.get(2).getChunkId());
        assertEquals(5L, selected.get(3).getChunkId());
    }

    @Test
    void diversifyByDoc_fillsRemainingFromOverflow() {
        List<AiKnowledgeChunkHitVo> hits = List.of(
                hit(1L, 10L),
                hit(2L, 10L),
                hit(3L, 10L));

        List<AiKnowledgeChunkHitVo> selected = AiKnowledgeRetrievalServiceImpl.diversifyByDoc(hits, 3, 2);

        assertEquals(3, selected.size());
        assertEquals(3L, selected.get(2).getChunkId());
    }

    @Test
    void mergeNeighborContent_joinsPrevSelfNext() {
        AiKnowledgeChunkHitVo self = hit(2L, 10L);
        self.setChunkIndex(2);
        self.setContent("步骤2");

        AiKnowledgeChunkHitVo prev = hit(1L, 10L);
        prev.setChunkIndex(1);
        prev.setContent("步骤1");

        AiKnowledgeChunkHitVo next = hit(3L, 10L);
        next.setChunkIndex(3);
        next.setContent("步骤3");

        String merged = AiKnowledgeRetrievalServiceImpl.mergeNeighborContent(
                self, Map.of(1, prev, 2, self, 3, next));

        assertTrue(merged.contains("步骤1"));
        assertTrue(merged.contains("步骤2"));
        assertTrue(merged.contains("步骤3"));
    }

    private static AiKnowledgeChunkHitVo hit(Long chunkId, Long docId) {
        AiKnowledgeChunkHitVo hit = new AiKnowledgeChunkHitVo();
        hit.setChunkId(chunkId);
        hit.setDocId(docId);
        hit.setContent("c" + chunkId);
        return hit;
    }
}
