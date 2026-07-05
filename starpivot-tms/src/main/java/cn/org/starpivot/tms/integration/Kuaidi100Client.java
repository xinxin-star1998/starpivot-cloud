package cn.org.starpivot.tms.integration;

import cn.org.starpivot.tms.config.TmsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Kuaidi100Client {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TmsProperties tmsProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isEnabled() {
        TmsProperties.Kuaidi100 cfg = tmsProperties.getKuaidi100();
        return cfg.isEnabled()
                && StringUtils.hasText(cfg.getCustomer())
                && StringUtils.hasText(cfg.getKey());
    }

    public QueryResult queryTrack(String com, String num) {
        if (!isEnabled()) {
            return QueryResult.empty();
        }
        try {
            String paramJson = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode().put("com", com).put("num", num));
            String sign = md5Upper(paramJson + tmsProperties.getKuaidi100().getKey()
                    + tmsProperties.getKuaidi100().getCustomer());
            String body = "customer="
                    + encode(tmsProperties.getKuaidi100().getCustomer())
                    + "&sign="
                    + encode(sign)
                    + "&param="
                    + encode(paramJson);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tmsProperties.getKuaidi100().getQueryUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Kuaidi100 query failed, status={}, num={}", response.statusCode(), num);
                return QueryResult.empty();
            }
            return parseResponse(response.body());
        } catch (Exception ex) {
            log.warn("Kuaidi100 query error, num={}", num, ex);
            return QueryResult.empty();
        }
    }

    private QueryResult parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (!"ok".equalsIgnoreCase(root.path("message").asText())) {
            log.debug("Kuaidi100 response not ok: {}", body);
            return QueryResult.empty();
        }
        String state = root.path("state").asText(null);
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            return new QueryResult(state, List.of());
        }
        List<TrackItem> items = new ArrayList<>();
        for (JsonNode node : data) {
            String timeText = node.path("ftime").asText(node.path("time").asText());
            LocalDateTime eventTime = parseTime(timeText);
            if (eventTime == null) {
                continue;
            }
            TrackItem item = new TrackItem();
            item.setEventTime(eventTime);
            item.setEventDesc(node.path("context").asText());
            item.setLocation(node.hasNonNull("location") ? node.path("location").asText() : null);
            item.setRawJson(node.toString());
            items.add(item);
        }
        return new QueryResult(state, items);
    }

    public String resolveShipmentStatus(String state) {
        if ("3".equals(state)) {
            return "DELIVERED";
        }
        if ("2".equals(state) || "4".equals(state) || "6".equals(state)) {
            return "EXCEPTION";
        }
        if (StringUtils.hasText(state)) {
            return "IN_TRANSIT";
        }
        return null;
    }

    private LocalDateTime parseTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim(), FMT);
        } catch (Exception ex) {
            return null;
        }
    }

    private String md5Upper(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash).toUpperCase();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Data
    public static class TrackItem {
        private LocalDateTime eventTime;
        private String eventDesc;
        private String location;
        private String rawJson;
    }

    public record QueryResult(String state, List<TrackItem> items) {
        public static QueryResult empty() {
            return new QueryResult(null, List.of());
        }
    }
}
