package cn.org.starpivot.ai.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTopicExtractorTest {

    private final ConversationTopicExtractor extractor = new ConversationTopicExtractor();

    @Test
    void extractsMenuPathAndQuotedTerms() {
        List<String> topics = extractor.extract("系统管理 → 用户管理 里怎么「新增用户」？");
        assertTrue(topics.stream().anyMatch(item -> item.contains("用户管理")));
        assertTrue(topics.contains("新增用户"));
    }

    @Test
    void extractsBacktickTerms() {
        List<String> topics = extractor.extract("权限里 `ai:knowledge:query` 怎么配？");
        assertTrue(topics.contains("ai:knowledge:query"));
    }
}
