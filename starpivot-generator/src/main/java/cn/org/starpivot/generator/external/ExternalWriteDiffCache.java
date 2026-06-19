package cn.org.starpivot.generator.external;

import cn.org.starpivot.generator.domain.dto.external.ExternalWriteDiffItemVO;
import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 会话级写盘 Diff 缓存（内存，避免懒加载单文件时重复 Velocity 渲染）
 */
@Component
public class ExternalWriteDiffCache {

    private record Entry(String cacheKey, Map<String, ExternalWriteDiffItemVO> byPath) {
    }

    private final Map<String, Entry> sessionEntries = new ConcurrentHashMap<>();

    public String buildCacheKey(List<String> tableNames, ExternalGenScope scope, Path outputRoot,
            String templateDir, GenPathProfile profile) {
        List<String> sorted = tableNames == null ? List.of() : tableNames.stream().sorted().toList();
        String scopeKey = scope == null ? "all" : scope.isGenBackend() + "," + scope.isGenFrontend() + "," + scope.isGenSql();
        String root = outputRoot == null ? "" : outputRoot.toAbsolutePath().normalize().toString();
        String tpl = templateDir == null ? "" : templateDir.trim();
        String profileKey = profileKey(profile);
        return String.join("|", String.join(",", sorted), scopeKey, root, tpl, profileKey);
    }

    private static String profileKey(GenPathProfile profile) {
        if (profile == null) {
            return "";
        }
        return String.join("/",
                nullToEmpty(profile.getBasePackage()),
                nullToEmpty(profile.getEntityPackage()),
                nullToEmpty(profile.getMapperPackage()),
                nullToEmpty(profile.getControllerPackage()),
                nullToEmpty(profile.getMapperXmlPath()),
                nullToEmpty(profile.getApiPath()),
                nullToEmpty(profile.getVuePagePath()),
                nullToEmpty(profile.getVueModulesPath()));
    }

    private static String nullToEmpty(String value) {
        return StringUtils.isEmpty(value) ? "" : value.trim();
    }

    public boolean matches(String sessionId, String cacheKey) {
        Entry entry = sessionEntries.get(sessionId);
        return entry != null && entry.cacheKey.equals(cacheKey);
    }

    public void put(String sessionId, String cacheKey, List<ExternalWriteDiffItemVO> items) {
        Map<String, ExternalWriteDiffItemVO> byPath = new LinkedHashMap<>();
        for (ExternalWriteDiffItemVO item : items) {
            byPath.put(item.getPath(), copyItem(item));
        }
        sessionEntries.put(sessionId, new Entry(cacheKey, Collections.unmodifiableMap(byPath)));
    }

    public List<ExternalWriteDiffItemVO> list(String sessionId) {
        Entry entry = sessionEntries.get(sessionId);
        if (entry == null) {
            return List.of();
        }
        return entry.byPath.values().stream().map(this::copyItem).collect(Collectors.toCollection(ArrayList::new));
    }

    public ExternalWriteDiffItemVO getFile(String sessionId, String path) {
        Entry entry = sessionEntries.get(sessionId);
        if (entry == null) {
            return null;
        }
        ExternalWriteDiffItemVO item = entry.byPath.get(path);
        return item == null ? null : copyItem(item);
    }

    public void invalidate(String sessionId) {
        if (sessionId != null) {
            sessionEntries.remove(sessionId);
        }
    }

    public static List<ExternalWriteDiffItemVO> summaryOnly(List<ExternalWriteDiffItemVO> items) {
        List<ExternalWriteDiffItemVO> summary = new ArrayList<>(items.size());
        for (ExternalWriteDiffItemVO item : items) {
            summary.add(ExternalWriteDiffItemVO.builder()
                    .path(item.getPath())
                    .status(item.getStatus())
                    .build());
        }
        return summary;
    }

    private ExternalWriteDiffItemVO copyItem(ExternalWriteDiffItemVO item) {
        return ExternalWriteDiffItemVO.builder()
                .path(item.getPath())
                .status(item.getStatus())
                .oldContent(item.getOldContent())
                .newContent(item.getNewContent())
                .build();
    }
}

