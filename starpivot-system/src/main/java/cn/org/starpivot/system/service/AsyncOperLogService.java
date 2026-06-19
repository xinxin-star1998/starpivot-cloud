package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOperLogService {

    private final SysOperLogMapper sysOperLogMapper;

    @Async("taskExecutor")
    public void saveOperLogAsync(SysOperLog operLog) {
        try {
            sysOperLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("异步日志保存失败 - 标题: {}, 用户: {}", operLog.getTitle(), operLog.getOperName(), e);
        }
    }
}
