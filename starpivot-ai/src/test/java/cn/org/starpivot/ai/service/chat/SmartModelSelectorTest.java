package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartModelSelectorTest {

    @Test
    void pick_usesYamlWhenCatalogEmpty() {
        AiProperties.QueryRouterProperties router = new AiProperties.QueryRouterProperties();
        router.setChatModel("deepseek-chat");
        router.setReasonerModel("deepseek-reasoner");

        assertEquals("deepseek-chat", SmartModelSelector.pick(ChatIntent.KNOWLEDGE, router, null));
        assertEquals("deepseek-reasoner", SmartModelSelector.pick(ChatIntent.REASONING, router, null));
    }

    @Test
    void pick_prefersMatchingIdsFromProviderCatalog() {
        AiProperties.QueryRouterProperties router = new AiProperties.QueryRouterProperties();
        router.setChatModel("deepseek-chat");
        router.setReasonerModel("deepseek-reasoner");
        AiRuntimeSnapshot runtime = AiRuntimeSnapshot.builder()
                .defaultModel("qwen-plus")
                .models(List.of(
                        AiModelVo.builder().id("qwen-plus").build(),
                        AiModelVo.builder().id("qwen3-coder-plus").build(),
                        AiModelVo.builder().id("qwen3.5-plus").build()))
                .build();

        assertEquals("qwen-plus", SmartModelSelector.pick(ChatIntent.GENERAL, router, runtime));
        assertEquals("qwen3-coder-plus", SmartModelSelector.pick(ChatIntent.DEVELOPER, router, runtime));
        assertEquals("qwen-plus", SmartModelSelector.pick(ChatIntent.REASONING, router, runtime));
    }
}
