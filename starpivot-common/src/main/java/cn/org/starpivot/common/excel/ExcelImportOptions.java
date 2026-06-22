package cn.org.starpivot.common.excel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel 导入选项（覆盖更新、业务扩展参数等）。
 * <p>
 * 通过 {@link #builder()} 或 {@link #of(boolean)} 构建，传递给
 * {@link ExcelBizHandler#importRows(java.util.List, ExcelImportOptions)}。
 * </p>
 */
public final class ExcelImportOptions {

    private final boolean updateSupport;
    private final Map<String, Object> params;

    private ExcelImportOptions(boolean updateSupport, Map<String, Object> params) {
        this.updateSupport = updateSupport;
        this.params = params != null ? Collections.unmodifiableMap(new HashMap<>(params)) : Map.of();
    }

    /**
     * 快速创建仅含覆盖开关的选项。
     *
     * @param updateSupport 是否允许按唯一键更新已有记录
     */
    public static ExcelImportOptions of(boolean updateSupport) {
        return new ExcelImportOptions(updateSupport, Map.of());
    }

    /** @return 流式构建器 */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 是否启用覆盖更新 */
    public boolean isUpdateSupport() {
        return updateSupport;
    }

    /** @return 不可变的扩展参数字典 */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 获取扩展参数原始值。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 {@code null}
     */
    public Object getParam(String key) {
        return params.get(key);
    }

    /**
     * 获取扩展参数并转为 {@link Integer}。
     *
     * @param key 参数键
     * @return 整数值，不存在时返回 {@code null}
     * @throws IllegalArgumentException 值无法转为整数时
     */
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

    /** {@link ExcelImportOptions} 构建器 */
    public static final class Builder {
        private boolean updateSupport;
        private final Map<String, Object> params = new HashMap<>();

        /** 设置是否覆盖更新已有记录 */
        public Builder updateSupport(boolean updateSupport) {
            this.updateSupport = updateSupport;
            return this;
        }

        /**
         * 添加扩展业务参数（键值均非空时生效）。
         *
         * @param key   参数键
         * @param value 参数值
         */
        public Builder param(String key, Object value) {
            if (key != null && value != null) {
                params.put(key, value);
            }
            return this;
        }

        /** @return 不可变 {@link ExcelImportOptions} 实例 */
        public ExcelImportOptions build() {
            return new ExcelImportOptions(updateSupport, params);
        }
    }
}
