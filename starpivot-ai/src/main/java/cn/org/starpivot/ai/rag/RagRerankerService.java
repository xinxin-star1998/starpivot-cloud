package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRerankerService {

    private final WebClient.Builder webClientBuilder;
    private final AiProperties aiProperties;

    @Value("${spring.ai.openai.embedding.api-key:}")
    private String embeddingApiKey;

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    public boolean isEnabled() {
        AiProperties.RerankerProperties reranker = aiProperties.getRag().getReranker();
        return reranker.isEnabled()
                && StringUtils.hasText(reranker.getEndpoint())
                && StringUtils.hasText(resolveApiKey(reranker));
    }

    public List<AiKnowledgeChunkHitVo> rerank(String question, List<AiKnowledgeChunkHitVo> candidates, int topN) {
        if (!isEnabled() || candidates.isEmpty()) {
            return candidates.stream().limit(topN).toList();
        }
        if (candidates.size() <= topN) {
            return candidates;
        }
        try {
            return callRerankApi(question, candidates, topN);
        } catch (Exception ex) {
            log.warn("[Reranker] failed, fallback to RRF order: {}", ex.getMessage());
            return candidates.stream().limit(topN).toList();
        }
    }

    private List<AiKnowledgeChunkHitVo> callRerankApi(
            String question, List<AiKnowledgeChunkHitVo> candidates, int topN) {
        AiProperties.RerankerProperties config = aiProperties.getRag().getReranker();
        List<String> docs = candidates.stream()
                .map(AiKnowledgeChunkHitVo::getContent)
                .collect(Collectors.toList());

        RerankRequest request = new RerankRequest();
        request.setModel(config.getModel());
        request.setInput(new RerankInput(question, docs));
        request.setParameters(new RerankParams(topN, false));

        WebClient client = webClientBuilder
                .baseUrl(config.getEndpoint())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + resolveApiKey(config))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        RerankResponse response = client.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RerankResponse.class)
                .timeout(Duration.ofMillis(config.getTimeoutMs()))
                .block();

        if (response == null || response.getOutput() == null || response.getOutput().getResults() == null) {
            throw new RuntimeException("Reranker API returned empty result");
        }

        List<AiKnowledgeChunkHitVo> reranked = response.getOutput().getResults().stream()
                .sorted(Comparator.comparingDouble(RerankResult::getRelevanceScore).reversed())
                .map(result -> {
                    AiKnowledgeChunkHitVo original = candidates.get(result.getIndex());
                    original.setScore(result.getRelevanceScore());
                    return original;
                })
                .collect(Collectors.toList());

        log.info("[Reranker] reranked {} -> {}", candidates.size(), reranked.size());
        return reranked;
    }

    private String resolveApiKey(AiProperties.RerankerProperties reranker) {
        if (StringUtils.hasText(reranker.getApiKey())) {
            return reranker.getApiKey().trim();
        }
        if (StringUtils.hasText(embeddingApiKey)) {
            return embeddingApiKey.trim();
        }
        if (StringUtils.hasText(openAiApiKey)) {
            return openAiApiKey.trim();
        }
        return "";
    }

    @Data
    static class RerankRequest {
        private String model;
        private RerankInput input;
        private RerankParams parameters;
    }

    @Data
    static class RerankInput {
        private String query;
        private List<String> documents;

        RerankInput(String query, List<String> documents) {
            this.query = query;
            this.documents = documents;
        }
    }

    @Data
    static class RerankParams {
        @JsonProperty("top_n")
        private int topN;
        @JsonProperty("return_documents")
        private boolean returnDocuments;

        RerankParams(int topN, boolean returnDocuments) {
            this.topN = topN;
            this.returnDocuments = returnDocuments;
        }
    }

    @Data
    static class RerankResponse {
        private RerankOutput output;
    }

    @Data
    static class RerankOutput {
        private List<RerankResult> results;
    }

    @Data
    static class RerankResult {
        private int index;
        @JsonProperty("relevance_score")
        private double relevanceScore;
    }
}
