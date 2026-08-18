package cn.org.starpivot.ai.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagQueryExpandUtilsTest {

    @Test
    void expand_keepsStandaloneQuery() {
        List<String> queries = RagQueryExpandUtils.expand("如何配置知识库");
        assertEquals(List.of("如何配置知识库"), queries);
    }

    @Test
    void expand_splitsCompoundConnectors() {
        List<String> queries = RagQueryExpandUtils.expand("如何配置权限以及菜单");
        assertEquals(3, queries.size());
        assertEquals("如何配置权限以及菜单", queries.get(0));
        assertTrue(queries.contains("如何配置权限"));
        assertTrue(queries.contains("菜单"));
    }

    @Test
    void expand_blankReturnsEmpty() {
        assertEquals(List.of(), RagQueryExpandUtils.expand("  "));
    }
}
