package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.bo.MessagePushPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MessageSseRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(ex -> remove(userId, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ex) {
            remove(userId, emitter);
        }
        return emitter;
    }

    public void dispatch(MessagePushPayload payload) {
        if (payload == null || payload.getUserId() == null) {
            return;
        }
        List<SseEmitter> connections = emitters.get(payload.getUserId());
        if (connections == null || connections.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : connections) {
            try {
                emitter.send(SseEmitter.event().name("message").data(payload));
            } catch (Exception ex) {
                remove(payload.getUserId(), emitter);
                log.debug("SSE push failed, userId={}", payload.getUserId(), ex);
            }
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            emitters.remove(userId, list);
        }
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // ignore
        }
    }
}
