package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.entity.AiProvider;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AiModelClientFactory aiModelClientFactory;

    private volatile WebClient rerankWebClient;
    private volatile String rerankClientKey;

    public boolean isEnabled() {
        AiProvider provider = aiModelClientFactory.rerankProvider();
        if (provider != null && StringUtils.hasText(resolveRerankEndpoint(provider))) {
            return true;
        }
        AiProperties.RerankerProperties reranker = aiProperties.getRag().getReranker();
        return reranker.isEnabled()
                && StringUtils.hasText(reranker.getEndpoint())
                && StringUtils.hasText(resolveYamlApiKey(reranker));
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
        AiProvider provider = aiModelClientFactory.rerankProvider();
        String endpoint;
        String apiKey;
        String model;
        long timeoutMs = aiProperties.getRag().getReranker().getTimeoutMs();
        if (provider != null && StringUtils.hasText(resolveRerankEndpoint(provider))) {
            endpoint = resolveRerankEndpoint(provider);
            apiKey = provider.getApiKey().trim();
            model = StringUtils.hasText(provider.getDefaultRerankModel())
                    ? provider.getDefaultRerankModel().trim()
                    : "gte-rerank";
        } else {
            AiProperties.RerankerProperties config = aiProperties.getRag().getReranker();
            endpoint = config.getEndpoint();
            apiKey = resolveYamlApiKey(config);
            model = config.getModel();
        }

        List<String> docs = candidates.stream()
                .map(AiKnowledgeChunkHitVo::getContent)
                .collect(Collectors.toList());

        RerankRequest request = new RerankRequest();
        request.setModel(model);
        request.setInput(new RerankInput(question, docs));
        request.setParameters(new RerankParams(topN, false));

        WebClient client = getRerankWebClient(endpoint, apiKey);

        RerankResponse response = client.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RerankResponse.class)
                .timeout(Duration.ofMillis(timeoutMs > 0 ? timeoutMs : 1500))
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

    private WebClient getRerankWebClient(String endpoint, String apiKey) {
        String clientKey = endpoint + "|" + apiKey;
        WebClient cached = rerankWebClient;
        if (cached != null && clientKey.equals(rerankClientKey)) {
            return cached;
        }
        synchronized (this) {
            if (rerankWebClient == null || !clientKey.equals(rerankClientKey)) {
                rerankWebClient = webClientBuilder
                        .baseUrl(endpoint)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
                rerankClientKey = clientKey;
            }
            return rerankWebClient;
        }
    }

    private String resolveRerankEndpoint(AiProvider provider) {
        if (provider != null && StringUtils.hasText(provider.getRerankEndpoint())) {
            return provider.getRerankEndpoint().trim();
        }
        return "";
    }

    private String resolveYamlApiKey(AiProperties.RerankerProperties reranker) {
        if (StringUtils.hasText(reranker.getApiKey())) {
            return reranker.getApiKey().trim();
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
