package cn.org.starpivot.file.task;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.file.config.FileCenterPurgeProperties;
import cn.org.starpivot.file.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 回收站超期文件物理清理定时任务。
 * <p>
 * 清理规则：{@code del_flag=2} 且 {@code delete_time} 早于保留期限的文件，
 * 先删 OSS 对象，再物理删除 DB 记录。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file-center.purge", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SysFileRecyclePurgeTask {

    private final ISysFileService sysFileService;
    private final FileCenterPurgeProperties purgeProperties;

    @DistributedScheduled(key = "file:recycle-purge", lockTtlSeconds = 3600)
    @Scheduled(cron = "${file-center.purge.cron:0 0 3 * * ?}")
    public void purgeExpiredRecycleFiles() {
        log.info("开始执行回收站物理清理任务，保留天数={}", purgeProperties.getRecycleRetentionDays());
        int purged = sysFileService.purgeExpiredRecycleFiles();
        log.info("回收站物理清理完成，共清理 {} 个文件", purged);
    }
}
