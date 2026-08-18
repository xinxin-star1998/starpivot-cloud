package cn.org.starpivot.ai.service.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseFormatGuideTest {

    @Test
    void forScene_supportContainsStepsGuidance() {
        String guide = ResponseFormatGuide.forScene("support");
        assertTrue(guide.contains("有序列表"));
        assertTrue(guide.contains("一级 → 二级"));
    }

    @Test
    void forScene_developerContainsCodeStructure() {
        String guide = ResponseFormatGuide.forScene("developer");
        assertTrue(guide.contains("先结论"));
        assertTrue(guide.contains("```"));
        assertTrue(guide.contains("必须有空格"));
    }

    @Test
    void forScene_defaultRequiresSpacedCodeTokens() {
        assertTrue(ResponseFormatGuide.forScene("default").contains("publicstaticvoid"));
    }

    @Test
    void ragInstruction_avoidsCitingMaterialNumbers() {
        assertTrue(ResponseFormatGuide.ragInstruction().contains("不要逐条照搬"));
    }

    @Test
    void agentInstruction_mentionsKnowledgeTool() {
        assertTrue(ResponseFormatGuide.agentInstruction().contains("searchKnowledgeBase"));
    }

    @Test
    void agentInstruction_withRagPrefersPreloadedContext() {
        String guide = ResponseFormatGuide.agentInstruction(true);
        assertTrue(guide.contains("参考资料已在下方提供"));
        assertTrue(guide.contains("一般无需再检索"));
    }

    @Test
    void emptyRagAbstain_warnsAgainstInvention() {
        assertTrue(ResponseFormatGuide.emptyRagAbstain().contains("不要编造"));
    }
}
