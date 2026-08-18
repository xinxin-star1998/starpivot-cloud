package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentSourceCollectorTest {

    @Test
    void merge_deduplicatesByChunkIdAndKeepsOrder() {
        RagSourceVo first = RagSourceVo.builder().chunkId(1L).docTitle("A").build();
        RagSourceVo duplicate = RagSourceVo.builder().chunkId(1L).docTitle("A-dup").build();
        RagSourceVo second = RagSourceVo.builder().chunkId(2L).docTitle("B").build();

        List<RagSourceVo> merged = AgentSourceCollector.merge(List.of(first), List.of(duplicate, second));

        assertEquals(2, merged.size());
        assertEquals("A", merged.get(0).getDocTitle());
        assertEquals("B", merged.get(1).getDocTitle());
    }

    @Test
    void drain_returnsCollectedSourcesOnce() {
        AgentSourceCollector collector = new AgentSourceCollector();
        collector.begin("c1");
        collector.addAll("c1", List.of(RagSourceVo.builder().chunkId(9L).build()));

        assertEquals(1, collector.drain("c1").size());
        assertEquals(0, collector.drain("c1").size());
    }
}
