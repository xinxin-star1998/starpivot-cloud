package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentSourceCollector {

    private final ConcurrentHashMap<String, List<RagSourceVo>> bag = new ConcurrentHashMap<>();

    public void begin(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        bag.put(conversationId, new ArrayList<>());
    }

    public void addAll(String conversationId, List<RagSourceVo> sources) {
        if (!StringUtils.hasText(conversationId) || sources == null || sources.isEmpty()) {
            return;
        }
        bag.compute(conversationId, (key, existing) -> {
            List<RagSourceVo> next = existing != null ? existing : new ArrayList<>();
            synchronized (next) {
                next.addAll(sources);
            }
            return next;
        });
    }

    public List<RagSourceVo> drain(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<RagSourceVo> removed = bag.remove(conversationId);
        if (removed == null || removed.isEmpty()) {
            return List.of();
        }
        synchronized (removed) {
            return List.copyOf(removed);
        }
    }

    public static List<RagSourceVo> merge(List<RagSourceVo> primary, List<RagSourceVo> extra) {
        Map<Long, RagSourceVo> unique = new LinkedHashMap<>();
        addUnique(unique, primary);
        addUnique(unique, extra);
        return List.copyOf(unique.values());
    }

    private static void addUnique(Map<Long, RagSourceVo> unique, List<RagSourceVo> sources) {
        if (sources == null) {
            return;
        }
        long synthetic = -1;
        for (RagSourceVo source : sources) {
            Long key = source.getChunkId() != null ? source.getChunkId() : synthetic--;
            unique.putIfAbsent(key, source);
        }
    }
}
