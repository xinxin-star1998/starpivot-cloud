package cn.org.starpivot.api.file;

import lombok.Getter;

/**
 * 文件媒体类型（与业务 Category 正交）。
 */
@Getter
public enum FileMediaType {

    IMAGE("IMAGE", "图片", 10L * 1024 * 1024),
    VIDEO("VIDEO", "视频", 200L * 1024 * 1024),
    DOCUMENT("DOCUMENT", "文档", 50L * 1024 * 1024),
    AUDIO("AUDIO", "音频", 50L * 1024 * 1024),
    OTHER("OTHER", "其他", 50L * 1024 * 1024);

    private final String code;
    private final String label;
    /** 默认单文件上限（字节） */
    private final long defaultMaxSize;

    FileMediaType(String code, String label, long defaultMaxSize) {
        this.code = code;
        this.label = label;
        this.defaultMaxSize = defaultMaxSize;
    }

    public static FileMediaType of(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        for (FileMediaType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
