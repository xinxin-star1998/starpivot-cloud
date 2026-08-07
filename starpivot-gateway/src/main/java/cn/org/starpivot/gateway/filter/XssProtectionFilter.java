package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.gateway.config.XssProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网关 XSS 防护全局过滤器（响应式）。
 * <p>
 * 对请求参数和 JSON Body 进行 XSS 清理，防止跨站脚本攻击。
 * 运行于 WebFlux {@link GlobalFilter} 上下文，使用 {@link ServerHttpRequestDecorator}
 * 包装原始请求以提供清理后的内容。
 * </p>
 * <ul>
 *   <li>Query 参数 — 清理所有参数值</li>
 *   <li>JSON Body — 递归清理所有字符串字段</li>
 *   <li>白名单路径 — 跳过清理</li>
 * </ul>
 *
 * @see XssProperties
 * @see XssCleaner
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XssProtectionFilter implements GlobalFilter, Ordered {

    private final XssProperties xssProperties;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用
        if (!xssProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // 检查白名单
        if (isWhitelisted(path)) {
            log.debug("XSS filter skipped for whitelisted path: {}", path);
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        MediaType contentType = request.getHeaders().getContentType();

        // 处理 JSON Body
        if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return processJsonBody(exchange, chain);
        }

        // 处理 Query 参数（GET 请求或无 Body 的请求）
        ServerHttpRequest cleanedRequest = cleanQueryParameters(request);
        return chain.filter(exchange.mutate().request(cleanedRequest).build());
    }

    /**
     * 处理 JSON Body：读取、清理、重新包装。
     */
    private Mono<Void> processJsonBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String originalBody = new String(bytes, StandardCharsets.UTF_8);
                    String cleanedBody = cleanJsonString(originalBody);

                    byte[] cleanedBytes = cleanedBody.getBytes(StandardCharsets.UTF_8);
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    DataBuffer cleanedBuffer = bufferFactory.wrap(cleanedBytes);

                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(cleanedBuffer);
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.putAll(super.getHeaders());
                            headers.setContentLength(cleanedBytes.length);
                            return headers;
                        }
                    };

                    return chain.filter(exchange.mutate().request(decorator).build());
                })
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    /**
     * 清理 Query 参数。
     */
    private ServerHttpRequest cleanQueryParameters(ServerHttpRequest request) {
        MultiValueMap<String, String> originalParams = request.getQueryParams();
        boolean hasChanges = false;

        Map<String, String[]> cleanedParams = new LinkedHashMap<>();
        for (Map.Entry<String, java.util.List<String>> entry : originalParams.entrySet()) {
            String key = XssCleaner.clean(entry.getKey());
            String[] values = entry.getValue().stream()
                    .map(v -> {
                        String cleaned = XssCleaner.clean(v);
                        return cleaned;
                    })
                    .toArray(String[]::new);
            cleanedParams.put(key, values);

            if (!key.equals(entry.getKey()) || !java.util.Arrays.equals(values,
                    entry.getValue().toArray(new String[0]))) {
                hasChanges = true;
            }
        }

        if (!hasChanges) {
            return request;
        }

        // 重建 URI
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(request.getURI());
        builder.replaceQuery(null);
        for (Map.Entry<String, String[]> entry : cleanedParams.entrySet()) {
            for (String value : entry.getValue()) {
                builder.queryParam(entry.getKey(), value);
            }
        }
        URI newUri = builder.build(true).toUri();

        return new ServerHttpRequestDecorator(request) {
            @Override
            public URI getURI() {
                return newUri;
            }

            @Override
            public MultiValueMap<String, String> getQueryParams() {
                MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
                cleanedParams.forEach((k, v) -> params.addAll(k, java.util.Arrays.asList(v)));
                return params;
            }
        };
    }

    /**
     * 清理 JSON 字符串中的所有字符串字段。
     *
     * @param jsonString 原始 JSON
     * @return 清理后的 JSON
     */
    private String cleanJsonString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            cleanJsonNode(root);
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.warn("XSS filter: failed to parse JSON, returning original. Error: {}", e.getMessage());
            return jsonString;
        }
    }

    /**
     * 递归清理 JsonNode 中的所有字符串值。
     */
    private void cleanJsonNode(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                if (value.isTextual()) {
                    String cleaned = XssCleaner.clean(value.asText());
                    objectNode.set(field.getKey(), new TextNode(cleaned));
                } else {
                    cleanJsonNode(value);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode element = arrayNode.get(i);
                if (element.isTextual()) {
                    String cleaned = XssCleaner.clean(element.asText());
                    arrayNode.set(i, new TextNode(cleaned));
                } else {
                    cleanJsonNode(element);
                }
            }
        }
    }

    /**
     * 检查路径是否匹配白名单。
     */
    private boolean isWhitelisted(String path) {
        return xssProperties.getWhitelist().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 过滤器顺序：在 StripUserHeadersFilter (+50) 之后、AuthGlobalFilter (+100) 之前执行。
     *
     * @return {@code HIGHEST_PRECEDENCE + 90}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 90;
    }
}
