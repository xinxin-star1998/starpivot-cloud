package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSseEmitterSupport {

    private final ObjectMapper objectMapper;

    public void sendMetaEvent(SseEmitter emitter, String conversationId, List<RagSourceVo> sources) {
        sendMetaEvent(emitter, conversationId, RagRetrievalResult.builder().sources(sources).build(), null);
    }

    public void sendMetaEvent(
            SseEmitter emitter, String conversationId, RagRetrievalResult ragResult, ChatExecutionPlan plan) {
        synchronized (emitter) {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("conversationId", conversationId);
                List<RagSourceVo> sources = ragResult != null ? ragResult.getSources() : null;
                if (sources != null && !sources.isEmpty()) {
                    payload.put("sources", sources);
                }
                if (ragResult != null && ragResult.isDegraded()) {
                    payload.put("ragDegraded", true);
                }
                if (plan != null) {
                    payload.put("promptScene", plan.promptScene());
                    payload.put("model", plan.model());
                    payload.put("routed", plan.autoScene() || plan.autoModel());
                    if (plan.intent() != null) {
                        payload.put("intent", plan.intent().name());
                    }
                }
                emitter.send(SseEmitter.event()
                        .name("meta")
                        .data(objectMapper.writeValueAsString(payload), MediaType.APPLICATION_JSON));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    public void sendStatusEvent(SseEmitter emitter, String phase, String message) {
        synchronized (emitter) {
            try {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("phase", phase);
                payload.put("message", message);
                emitter.send(
                        SseEmitter.event()
                                .name("status")
                                .data(objectMapper.writeValueAsString(payload), MediaType.APPLICATION_JSON));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    public void sendDeltaEvent(SseEmitter emitter, String chunk) {
        synchronized (emitter) {
            try {
                emitter.send(SseEmitter.event().name("delta").data(chunk));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    public void handleStreamError(SseEmitter emitter, Throwable error) {
        log.warn("AI chat stream failed", error);
        synchronized (emitter) {
            try {
                String message = error.getMessage() != null ? error.getMessage() : "AI 生成失败";
                emitter.send(SseEmitter.event().name("error").data(message));
                emitter.complete();
            } catch (IOException ignored) {
                emitter.completeWithError(error);
            }
        }
    }

    public void completeStream(SseEmitter emitter) {
        synchronized (emitter) {
            try {
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }
}
