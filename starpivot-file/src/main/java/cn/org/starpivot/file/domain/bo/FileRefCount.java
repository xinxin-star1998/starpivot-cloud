package cn.org.starpivot.file.domain.bo;

import lombok.Data;

/**
 * 文件业务引用计数统计。
 */
@Data
public class FileRefCount {

    private Long fileId;

    private Long refCount;
}
