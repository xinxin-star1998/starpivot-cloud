package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.entity.AiIndexTask;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;
import cn.org.starpivot.ai.mapper.AiIndexTaskMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeDocumentMapper;
import cn.org.starpivot.ai.service.AiIndexService;
import cn.org.starpivot.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiIndexServiceImpl implements AiIndexService {

    public static final String TASK_TYPE_TEXT = "INDEX_TEXT";
    public static final String TASK_TYPE_FILE = "INDEX_FILE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String INDEX_PENDING = "PENDING";
    public static final String INDEX_PROCESSING = "PROCESSING";
    public static final String INDEX_DONE = "DONE";
    public static final String INDEX_FAILED = "FAILED";

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiIndexTaskMapper aiIndexTaskMapper;
    private final AiIndexAsyncExecutor aiIndexAsyncExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTextIndex(Long docId) {
        releaseStuckIndexTasks(docId);
        assertNoActiveIndexTask(docId);
        createTask(docId, TASK_TYPE_TEXT);
        aiIndexAsyncExecutor.executeAsync(requireDocument(docId).getDocId(), latestTaskId(docId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFileIndex(Long docId) {
        releaseStuckIndexTasks(docId);
        assertNoActiveIndexTask(docId);
        createTask(docId, TASK_TYPE_FILE);
        aiIndexAsyncExecutor.executeAsync(docId, latestTaskId(docId));
    }

    @Override
    public void executeTask(Long taskId, Long docId) {
        aiIndexAsyncExecutor.executeAsync(docId, taskId);
    }

    @Override
    public AiKnowledgeDocument requireReadyDocument(Long docId) {
        AiKnowledgeDocument doc = requireDocument(docId);
        if (INDEX_PROCESSING.equals(doc.getIndexStatus())) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
        return doc;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceResetIndexState(Long docId) {
        List<AiIndexTask> activeTasks = aiIndexTaskMapper.selectList(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .in(AiIndexTask::getStatus, STATUS_PENDING, STATUS_RUNNING));
        LocalDateTime now = LocalDateTime.now();
        for (AiIndexTask task : activeTasks) {
            task.setStatus(STATUS_FAILED);
            task.setErrorMsg("索引任务已重置");
            task.setFinishedAt(now);
            aiIndexTaskMapper.updateById(task);
        }
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc != null
                && (INDEX_PENDING.equals(doc.getIndexStatus()) || INDEX_PROCESSING.equals(doc.getIndexStatus()))) {
            doc.setIndexStatus(INDEX_FAILED);
            doc.setErrorMsg("索引任务已重置，请重新提交");
            doc.setUpdateTime(now);
            aiKnowledgeDocumentMapper.updateById(doc);
        }
    }

    /** 清理异步未启动或超时的僵尸任务，避免阻塞后续索引 */
    private void releaseStuckIndexTasks(Long docId) {
        LocalDateTime pendingStaleBefore = LocalDateTime.now().minusSeconds(30);
        LocalDateTime runningStaleBefore = LocalDateTime.now().minusMinutes(10);
        List<AiIndexTask> stuckTasks = aiIndexTaskMapper.selectList(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .and(wrapper -> wrapper
                        .nested(pending -> pending
                                .eq(AiIndexTask::getStatus, STATUS_PENDING)
                                .isNull(AiIndexTask::getStartedAt)
                                .lt(AiIndexTask::getCreateTime, pendingStaleBefore))
                        .or(running -> running
                                .eq(AiIndexTask::getStatus, STATUS_RUNNING)
                                .lt(AiIndexTask::getStartedAt, runningStaleBefore))));
        LocalDateTime now = LocalDateTime.now();
        boolean resetDocStatus = false;
        for (AiIndexTask task : stuckTasks) {
            task.setStatus(STATUS_FAILED);
            task.setErrorMsg("索引任务超时已取消");
            task.setFinishedAt(now);
            aiIndexTaskMapper.updateById(task);
            resetDocStatus = true;
        }
        if (resetDocStatus) {
            Long activeCount = aiIndexTaskMapper.selectCount(new LambdaQueryWrapper<AiIndexTask>()
                    .eq(AiIndexTask::getDocId, docId)
                    .in(AiIndexTask::getStatus, STATUS_PENDING, STATUS_RUNNING));
            if (activeCount == null || activeCount == 0) {
                AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
                if (doc != null && INDEX_PROCESSING.equals(doc.getIndexStatus())) {
                    doc.setIndexStatus(INDEX_FAILED);
                    doc.setErrorMsg("索引任务超时已取消，请重新提交");
                    doc.setUpdateTime(now);
                    aiKnowledgeDocumentMapper.updateById(doc);
                }
            }
        }
    }

    private void assertNoActiveIndexTask(Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc != null && INDEX_PROCESSING.equals(doc.getIndexStatus())) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
        Long activeCount = aiIndexTaskMapper.selectCount(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .in(AiIndexTask::getStatus, STATUS_PENDING, STATUS_RUNNING));
        if (activeCount != null && activeCount > 0) {
            throw new BizException("文档正在索引中，请稍后再试");
        }
    }

    private void createTask(Long docId, String taskType) {
        AiIndexTask task = new AiIndexTask();
        task.setDocId(docId);
        task.setTaskType(taskType);
        task.setStatus(STATUS_PENDING);
        task.setRetryCount(0);
        task.setCreateTime(LocalDateTime.now());
        aiIndexTaskMapper.insert(task);

        AiKnowledgeDocument doc = requireDocument(docId);
        doc.setIndexStatus(INDEX_PENDING);
        doc.setErrorMsg(null);
        doc.setUpdateTime(LocalDateTime.now());
        aiKnowledgeDocumentMapper.updateById(doc);
    }

    private Long latestTaskId(Long docId) {
        AiIndexTask task = aiIndexTaskMapper.selectOne(new LambdaQueryWrapper<AiIndexTask>()
                .eq(AiIndexTask::getDocId, docId)
                .orderByDesc(AiIndexTask::getTaskId)
                .last("LIMIT 1"));
        if (task == null) {
            throw new BizException("索引任务不存在");
        }
        return task.getTaskId();
    }

    private AiKnowledgeDocument requireDocument(Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        return doc;
    }
}
