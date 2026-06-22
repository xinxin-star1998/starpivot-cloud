package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步操作日志服务类。
 * <p>
 * 将 {@link cn.org.starpivot.system.config.LogAspect} 采集的操作日志异步写入数据库，
 * 避免阻塞主请求线程。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务组件</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOperLogService {

    private final SysOperLogMapper sysOperLogMapper;

    /**
     * 异步保存操作日志记录。
     * <p>{@link Async} 指定使用 {@code taskExecutor} 线程池执行。</p>
     *
     * @param operLog 操作日志实体
     */
    @Async("taskExecutor")
    public void saveOperLogAsync(SysOperLog operLog) {
        try {
            sysOperLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("异步日志保存失败 - 标题: {}, 用户: {}", operLog.getTitle(), operLog.getOperName(), e);
        }
    }
}
