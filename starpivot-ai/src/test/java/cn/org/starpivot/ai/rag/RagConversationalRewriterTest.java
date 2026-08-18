package cn.org.starpivot.ai.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagConversationalRewriterTest {

    @Test
    void shouldRewrite_followUpPronouns() {
        assertTrue(RagConversationalRewriter.shouldRewrite("那怎么配置？", 2));
        assertTrue(RagConversationalRewriter.shouldRewrite("这个在哪打开", 4));
    }

    @Test
    void shouldRewrite_shortFollowUp() {
        assertTrue(RagConversationalRewriter.shouldRewrite("还有呢", 2));
    }

    @Test
    void shouldRewrite_skipsFirstTurnAndStandaloneWithoutPronouns() {
        assertFalse(RagConversationalRewriter.shouldRewrite("如何配置 RAG 知识库？", 0));
        assertFalse(RagConversationalRewriter.shouldRewrite("简单说说权限配置步骤", 4));
        assertFalse(RagConversationalRewriter.shouldRewrite(
                "请详细说明 StarPivot 后台如何创建知识库并上传 PDF 文档完成向量化", 4));
    }
}
