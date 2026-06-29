package cn.org.starpivot.file.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.file.domain.entity.SysFile;
import cn.org.starpivot.file.domain.entity.SysFileRef;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.mapper.SysFileRefMapper;
import cn.org.starpivot.file.service.ISysFileRefService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件业务引用服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileRefServiceImpl extends ServiceImpl<SysFileRefMapper, SysFileRef>
        implements ISysFileRefService {

    private final SysFileMapper sysFileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bind(Long fileId, String bizType, String bizId) {
        validateParams(fileId, bizType, bizId);

        long exists = count(new LambdaQueryWrapper<SysFileRef>()
                .eq(SysFileRef::getFileId, fileId)
                .eq(SysFileRef::getBizType, bizType.trim())
                .eq(SysFileRef::getBizId, bizId.trim()));
        if (exists > 0) {
            return;
        }

        SysFileRef ref = new SysFileRef();
        ref.setFileId(fileId);
        ref.setBizType(bizType.trim());
        ref.setBizId(bizId.trim());
        ref.setCreateBy(SecurityContextUtils.getUsername());
        ref.setCreateTime(LocalDateTime.now());
        save(ref);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long fileId, String bizType, String bizId) {
        validateParams(fileId, bizType, bizId);
        remove(new LambdaQueryWrapper<SysFileRef>()
                .eq(SysFileRef::getFileId, fileId)
                .eq(SysFileRef::getBizType, bizType.trim())
                .eq(SysFileRef::getBizId, bizId.trim()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByObjectNames(String bizType, String bizId, Collection<String> objectNames) {
        validateBizParams(bizType, bizId);
        unbindAllByBiz(bizType, bizId);
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        Set<String> distinct = objectNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String objectName : distinct) {
            bindByObjectName(objectName, bizType, bizId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAllByBiz(String bizType, String bizId) {
        validateBizParams(bizType, bizId);
        remove(new LambdaQueryWrapper<SysFileRef>()
                .eq(SysFileRef::getBizType, bizType.trim())
                .eq(SysFileRef::getBizId, bizId.trim()));
    }

    @Override
    public long countByFileId(Long fileId) {
        if (fileId == null) {
            return 0;
        }
        return baseMapper.countByFileId(fileId);
    }

    @Override
    public Map<Long, Long> countByFileIds(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return baseMapper.countGroupByFileIds(fileIds).stream()
                .collect(Collectors.toMap(
                        item -> item.getFileId(),
                        item -> item.getRefCount() != null ? item.getRefCount() : 0L,
                        (left, right) -> left));
    }

    @Override
    public void assertNoReference(Long fileId) {
        long count = countByFileId(fileId);
        if (count > 0) {
            throw new BizException(ErrorCode.BIZ_ERROR,
                    String.format("文件仍被 %d 个业务引用，请先解绑后再删除", count));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFileIds(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        baseMapper.deleteByFileIds(fileIds);
    }

    private void validateParams(Long fileId, String bizType, String bizId) {
        AssertUtils.notNull(fileId, ErrorCode.PARAM_INVALID, "文件ID不能为空");
        validateBizParams(bizType, bizId);
    }

    private void validateBizParams(String bizType, String bizId) {
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "业务类型与业务ID不能为空");
        }
    }

    private void bindByObjectName(String objectName, String bizType, String bizId) {
        SysFile file = sysFileMapper.selectActiveByObjectName(objectName);
        if (file == null || file.getFileId() == null) {
            log.debug("未找到文件元数据，跳过引用绑定: objectName={}", objectName);
            return;
        }
        bind(file.getFileId(), bizType, bizId);
    }
}
