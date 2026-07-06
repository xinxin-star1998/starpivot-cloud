package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;

public interface AiIndexService {

    void submitTextIndex(Long docId);

    void submitFileIndex(Long docId);

    void executeTask(Long taskId, Long docId);

    AiKnowledgeDocument requireReadyDocument(Long docId);

    /** 重建索引前清理卡住的 PENDING/RUNNING 任务 */
    void forceResetIndexState(Long docId);
}
