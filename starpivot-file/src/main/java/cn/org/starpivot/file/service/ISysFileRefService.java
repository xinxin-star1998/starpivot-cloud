package cn.org.starpivot.file.service;

/**
 * 文件业务引用服务。
 */
public interface ISysFileRefService {

    void bind(Long fileId, String bizType, String bizId);

    void unbind(Long fileId, String bizType, String bizId);

    void syncByObjectNames(String bizType, String bizId, java.util.Collection<String> objectNames);

    void unbindAllByBiz(String bizType, String bizId);

    long countByFileId(Long fileId);

    java.util.Map<Long, Long> countByFileIds(java.util.Collection<Long> fileIds);

    void assertNoReference(Long fileId);

    void deleteByFileIds(java.util.Collection<Long> fileIds);
}
