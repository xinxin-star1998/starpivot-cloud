package cn.org.starpivot.common.excel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel 导入选项（覆盖更新、业务扩展参数等）
 */
public final class ExcelImportOptions {

    private final boolean updateSupport;
    private final Map<String, Object> params;

    private ExcelImportOptions(boolean updateSupport, Map<String, Object> params) {
        this.updateSupport = updateSupport;
        this.params = params != null ? Collections.unmodifiableMap(new HashMap<>(params)) : Map.of();
    }

    public static ExcelImportOptions of(boolean updateSupport) {
        return new ExcelImportOptions(updateSupport, Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isUpdateSupport() {
        return updateSupport;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public Integer getIntParam(String key) {
        Object v = params.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s.trim());
        }
        throw new IllegalArgumentException("参数 " + key + " 无法转为整数");
    }

    public static final class Builder {
        private boolean updateSupport;
        private final Map<String, Object> params = new HashMap<>();

        public Builder updateSupport(boolean updateSupport) {
            this.updateSupport = updateSupport;
            return this;
        }

        public Builder param(String key, Object value) {
            if (key != null && value != null) {
                params.put(key, value);
            }
            return this;
        }

        public ExcelImportOptions build() {
            return new ExcelImportOptions(updateSupport, params);
        }
    }
}
