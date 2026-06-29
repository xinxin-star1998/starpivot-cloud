package cn.org.starpivot.file.service.impl;

import cn.org.starpivot.api.file.FileBizConstants;
import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.api.file.FileMediaType;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.storage.FileCenterUploadHelper;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.UploadResult;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.file.config.FileCenterProperties;
import cn.org.starpivot.file.config.FileCenterPurgeProperties;
import cn.org.starpivot.file.domain.bo.SysFileVO;
import cn.org.starpivot.file.domain.dto.SysFileQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileRecycleQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileUploadDTO;
import cn.org.starpivot.file.domain.entity.SysFile;
import cn.org.starpivot.file.domain.entity.SysFileFolder;
import cn.org.starpivot.file.mapper.SysFileFolderMapper;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.service.FileCategoryAccessService;
import cn.org.starpivot.file.service.ISysFileRefService;
import cn.org.starpivot.file.service.ISysFileService;
import cn.org.starpivot.file.support.FileHashUtils;
import cn.org.starpivot.file.support.FileMediaTypeResolver;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件中心服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private final SysFileFolderMapper sysFileFolderMapper;
    private final FileCenterUploadHelper fileCenterUploadHelper;
    private final FileMediaTypeResolver fileMediaTypeResolver;
    private final FileStorageService fileStorageService;
    private final FileCenterProperties fileCenterProperties;
    private final FileCenterPurgeProperties purgeProperties;
    private final FileCategoryAccessService fileCategoryAccessService;
    private final ISysFileRefService sysFileRefService;

    @Value("${oss.enabled:true}")
    private boolean ossEnabled;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFileVO upload(MultipartFile file, Long folderId, SysFileUploadDTO uploadDTO) {
        AssertUtils.notNull(folderId, ErrorCode.PARAM_INVALID, "文件夹ID不能为空");
        SysFileFolder folder = sysFileFolderMapper.selectById(folderId);
        AssertUtils.notNull(folder, ErrorCode.NOT_FOUND, "文件夹不存在");

        FileCategory category = FileCategory.of(folder.getCategory());
        fileCategoryAccessService.requireAccess(category.getCode());
        FileMediaType mediaType = fileMediaTypeResolver.resolve(file);
        validateFileSize(file, mediaType);

        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件名不能为空");
        }
        String trimmedName = originalName.trim();
        validateDisplayFileName(trimmedName);
        String fileExt = fileMediaTypeResolver.extractExtension(trimmedName);
        String displayName = normalizeDisplayFileName(trimmedName, fileExt);
        assertUniqueFileName(folderId, displayName, null);

        String fileHash = FileHashUtils.sha256Hex(file);
        SysFile existing = baseMapper.selectActiveByHash(fileHash);
        if (existing != null) {
            return saveInstantUpload(
                    file, folderId, category, mediaType, displayName, fileExt, fileHash, existing, uploadDTO);
        }

        UploadResult uploadResult;
        try {
            uploadResult = fileCenterUploadHelper.upload(
                    file, category.getObjectPathSegment(), folderId);
        } catch (Exception e) {
            log.error("文件上传失败, folderId={}", folderId, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "文件上传失败，请稍后重试");
        }

        SysFile entity = new SysFile();
        entity.setFolderId(folderId);
        entity.setCategory(category.getCode());
        entity.setMediaType(mediaType.getCode());
        entity.setFileName(displayName);
        entity.setFileExt(StringUtils.hasText(fileExt) ? fileExt : fileMediaTypeResolver.extractExtension(displayName));
        entity.setContentType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setObjectName(uploadResult.getObjectName());
        entity.setFileHash(fileHash);
        entity.setStorageProvider(ossEnabled ? "OSS" : "LOCAL");
        entity.setDelFlag(FileBizConstants.DEL_FLAG_NORMAL);
        entity.setCreateBy(SecurityContextUtils.getUsername());
        entity.setCreateTime(LocalDateTime.now());

        if (uploadDTO != null) {
            entity.setBizType(uploadDTO.getBizType());
            entity.setBizId(uploadDTO.getBizId());
            entity.setRemark(uploadDTO.getRemark());
        }

        try {
            save(entity);
        } catch (RuntimeException e) {
            compensateUploadedObject(uploadResult.getObjectName());
            throw e;
        }
        bindBizRefIfPresent(entity.getFileId(), uploadDTO);
        return toVO(entity, uploadResult.getPresignedUrl(), false);
    }

    private SysFileVO saveInstantUpload(
            MultipartFile file,
            Long folderId,
            FileCategory category,
            FileMediaType mediaType,
            String displayName,
            String fileExt,
            String fileHash,
            SysFile existing,
            SysFileUploadDTO uploadDTO) {
        SysFile entity = new SysFile();
        entity.setFolderId(folderId);
        entity.setCategory(category.getCode());
        entity.setMediaType(mediaType.getCode());
        entity.setFileName(displayName);
        entity.setFileExt(StringUtils.hasText(fileExt) ? fileExt : fileMediaTypeResolver.extractExtension(displayName));
        entity.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : existing.getContentType());
        entity.setFileSize(file.getSize());
        entity.setObjectName(existing.getObjectName());
        entity.setFileHash(fileHash);
        entity.setStorageProvider(existing.getStorageProvider());
        entity.setDelFlag(FileBizConstants.DEL_FLAG_NORMAL);
        entity.setCreateBy(SecurityContextUtils.getUsername());
        entity.setCreateTime(LocalDateTime.now());

        if (uploadDTO != null) {
            entity.setBizType(uploadDTO.getBizType());
            entity.setBizId(uploadDTO.getBizId());
            entity.setRemark(uploadDTO.getRemark());
        }

        save(entity);
        bindBizRefIfPresent(entity.getFileId(), uploadDTO);

        String presignedUrl = null;
        try {
            presignedUrl = fileStorageService.getPresignedUrl(existing.getObjectName());
        } catch (Exception e) {
            log.warn("秒传文件生成预览链接失败, fileId={}, objectName={}",
                    entity.getFileId(), existing.getObjectName(), e);
        }
        return toVO(entity, presignedUrl, true);
    }

    @Override
    public PageResponse<SysFileVO> pageList(SysFileQueryDTO queryDTO) {
        applyAccessibleCategories(queryDTO);
        if (StringUtils.hasText(queryDTO.getCategory())) {
            fileCategoryAccessService.requireAccess(queryDTO.getCategory());
        }
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysFile> result = baseMapper.selectPageList(page, queryDTO);
        return toPageResponse(result, true);
    }

    @Override
    public SysFileVO getDetail(Long fileId) {
        SysFileVO vo = toVO(requireActiveFile(fileId), null, false);
        vo.setRefCount(sysFileRefService.countByFileId(fileId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDelete(List<Long> ids) {
        AssertUtils.notEmpty(ids, ErrorCode.PARAM_INVALID, "删除ID不能为空");
        List<SysFile> files = listActiveFiles(ids);
        if (files.size() != ids.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分文件不存在或已在回收站");
        }
        assertCategoryAccess(files);
        for (SysFile file : files) {
            sysFileRefService.assertNoReference(file.getFileId());
        }

        String username = SecurityContextUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();
        update(new LambdaUpdateWrapper<SysFile>()
                .in(SysFile::getFileId, ids)
                .eq(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_NORMAL)
                .set(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_RECYCLE)
                .set(SysFile::getDeleteBy, username)
                .set(SysFile::getDeleteTime, now));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(List<Long> ids) {
        List<Long> distinctIds = normalizeIds(ids);
        AssertUtils.notEmpty(distinctIds, ErrorCode.PARAM_INVALID, "恢复ID不能为空");

        List<SysFile> recycleFiles = listRecycleFiles(distinctIds);
        if (recycleFiles.size() != distinctIds.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分文件不在回收站或不存在");
        }
        assertCategoryAccess(recycleFiles);

        Set<Long> folderIds = recycleFiles.stream()
                .map(SysFile::getFolderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!folderIds.isEmpty()) {
            long existingFolders = sysFileFolderMapper.selectBatchIds(folderIds).size();
            if (existingFolders != folderIds.size()) {
                throw new BizException(ErrorCode.NOT_FOUND, "目标文件夹不存在，无法恢复文件");
            }
        }

        int restored = baseMapper.restoreByIds(distinctIds, SecurityContextUtils.getUsername());
        if (restored != distinctIds.size()) {
            throw new BizException(ErrorCode.BIZ_ERROR, "部分文件恢复失败");
        }
    }

    @Override
    public PageResponse<SysFileVO> recyclePage(SysFileRecycleQueryDTO queryDTO) {
        applyAccessibleCategories(queryDTO);
        if (StringUtils.hasText(queryDTO.getCategory())) {
            fileCategoryAccessService.requireAccess(queryDTO.getCategory());
        }
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysFile> result = baseMapper.selectRecyclePageList(page, queryDTO);
        return toPageResponse(result, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToFolder(List<Long> ids, Long targetFolderId) {
        AssertUtils.notEmpty(ids, ErrorCode.PARAM_INVALID, "文件ID不能为空");
        AssertUtils.notNull(targetFolderId, ErrorCode.PARAM_INVALID, "目标文件夹不能为空");

        SysFileFolder targetFolder = sysFileFolderMapper.selectById(targetFolderId);
        AssertUtils.notNull(targetFolder, ErrorCode.NOT_FOUND, "目标文件夹不存在");
        fileCategoryAccessService.requireAccess(targetFolder.getCategory());

        String category = targetFolder.getCategory();
        String username = SecurityContextUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();

        List<SysFile> files = listActiveFiles(ids);
        if (files.size() != ids.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分文件不存在或已在回收站");
        }
        assertCategoryAccess(files);

        int moved = 0;
        for (SysFile file : files) {
            if (targetFolderId.equals(file.getFolderId())) {
                continue;
            }
            if (!category.equalsIgnoreCase(file.getCategory())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "不可跨业务分类迁移文件");
            }
            update(new LambdaUpdateWrapper<SysFile>()
                    .eq(SysFile::getFileId, file.getFileId())
                    .set(SysFile::getFolderId, targetFolderId)
                    .set(SysFile::getCategory, category)
                    .set(SysFile::getUpdateBy, username)
                    .set(SysFile::getUpdateTime, now));
            moved++;
        }

        if (moved == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "没有可迁移的文件（可能已在目标文件夹）");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(Long fileId, String fileName) {
        AssertUtils.notNull(fileId, ErrorCode.PARAM_INVALID, "文件ID不能为空");
        if (!StringUtils.hasText(fileName)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件名不能为空");
        }

        String trimmed = fileName.trim();
        validateDisplayFileName(trimmed);

        SysFile file = requireActiveFile(fileId);

        String finalName = normalizeDisplayFileName(trimmed, file.getFileExt());
        if (finalName.equals(file.getFileName())) {
            return;
        }

        long duplicate = count(new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getFolderId, file.getFolderId())
                .eq(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_NORMAL)
                .eq(SysFile::getFileName, finalName)
                .ne(SysFile::getFileId, fileId));
        if (duplicate > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "当前文件夹下已存在同名文件");
        }
        String newExt = fileMediaTypeResolver.extractExtension(finalName);
        update(new LambdaUpdateWrapper<SysFile>()
                .eq(SysFile::getFileId, fileId)
                .set(SysFile::getFileName, finalName)
                .set(SysFile::getFileExt, StringUtils.hasText(newExt) ? newExt : file.getFileExt())
                .set(SysFile::getUpdateBy, SecurityContextUtils.getUsername())
                .set(SysFile::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public Map<String, String> previewUrl(Long fileId) {
        SysFile file = requireActiveFile(fileId);
        if (!FileBizConstants.DEL_FLAG_NORMAL.equals(file.getDelFlag())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "已删除文件不可预览");
        }
        try {
            String url = fileStorageService.getPresignedUrl(file.getObjectName());
            return Map.of("url", url, "objectName", file.getObjectName());
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "获取预览链接失败");
        }
    }

    private void validateFileSize(MultipartFile file, FileMediaType mediaType) {
        Long maxSize = fileCenterProperties.getMaxSizeByMediaType()
                .getOrDefault(mediaType.getCode(), mediaType.getDefaultMaxSize());
        if (file.getSize() > maxSize) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    String.format("文件大小超过限制（最大 %d MB）", maxSize / 1024 / 1024));
        }
    }

    private void validateDisplayFileName(String fileName) {
        if (fileName.length() > 255) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件名长度不能超过255");
        }
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件名不能包含路径分隔符");
        }
    }

    /** 若用户未带扩展名，则保留原扩展名。 */
    private String normalizeDisplayFileName(String input, String originalExt) {
        if (fileMediaTypeResolver.extractExtension(input).length() > 0) {
            return input;
        }
        if (StringUtils.hasText(originalExt)) {
            return input + "." + originalExt;
        }
        return input;
    }

    private PageResponse<SysFileVO> toPageResponse(IPage<SysFile> page, boolean includeDisplayUrl) {
        List<SysFile> records = page.getRecords();
        Map<Long, String> folderNameMap = loadFolderNameMap(records);
        Map<String, String> displayUrlMap = includeDisplayUrl ? batchResolveDisplayUrls(records) : Collections.emptyMap();
        Map<Long, Long> refCountMap = loadRefCountMap(records);

        PageResponse<SysFileVO> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setPageCount(page.getPages());
        response.setRows(records.stream()
                .map(file -> buildVO(file, null, folderNameMap, displayUrlMap, refCountMap, includeDisplayUrl, false))
                .collect(Collectors.toList()));
        return response;
    }

    private Map<Long, Long> loadRefCountMap(List<SysFile> files) {
        List<Long> fileIds = files.stream()
                .map(SysFile::getFileId)
                .filter(Objects::nonNull)
                .toList();
        if (fileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysFileRefService.countByFileIds(fileIds);
    }

    private Map<Long, String> loadFolderNameMap(List<SysFile> files) {
        Set<Long> folderIds = files.stream()
                .map(SysFile::getFolderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (folderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysFileFolderMapper.selectBatchIds(folderIds).stream()
                .collect(Collectors.toMap(
                        SysFileFolder::getFolderId,
                        SysFileFolder::getFolderName,
                        (left, right) -> left));
    }

    /** 列表场景批量签名：仅图片类型需要缩略图 URL。 */
    private Map<String, String> batchResolveDisplayUrls(List<SysFile> files) {
        List<String> objectNames = files.stream()
                .filter(file -> FileMediaType.IMAGE.getCode().equals(file.getMediaType()))
                .map(SysFile::getObjectName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (objectNames.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return fileStorageService.getPresignedUrls(objectNames);
        } catch (Exception e) {
            return objectNames.stream()
                    .collect(Collectors.toMap(
                            objectName -> objectName,
                            fileStorageService::getPermanentUrl,
                            (left, right) -> left));
        }
    }

    private SysFileVO toVO(SysFile file, String presignedUrl, boolean instantUpload) {
        Map<Long, String> folderNameMap = Collections.emptyMap();
        if (file.getFolderId() != null) {
            SysFileFolder folder = sysFileFolderMapper.selectById(file.getFolderId());
            if (folder != null) {
                folderNameMap = Map.of(folder.getFolderId(), folder.getFolderName());
            }
        }
        return buildVO(file, presignedUrl, folderNameMap, Collections.emptyMap(), Collections.emptyMap(), true, instantUpload);
    }

    private SysFileVO buildVO(
            SysFile file,
            String presignedUrl,
            Map<Long, String> folderNameMap,
            Map<String, String> displayUrlMap,
            Map<Long, Long> refCountMap,
            boolean includeDisplayUrl,
            boolean instantUpload) {
        SysFileVO vo = new SysFileVO();
        BeanUtils.copyProperties(file, vo);
        vo.setInstantUpload(instantUpload);
        if (file.getFileId() != null) {
            vo.setRefCount(refCountMap.getOrDefault(file.getFileId(), 0L));
        }

        try {
            FileCategory category = FileCategory.of(file.getCategory());
            vo.setCategoryLabel(category.getLabel());
        } catch (IllegalArgumentException ignored) {
            vo.setCategoryLabel(file.getCategory());
        }

        FileMediaType mediaType = FileMediaType.of(file.getMediaType());
        vo.setMediaTypeLabel(mediaType.getLabel());
        vo.setPreviewMode(resolvePreviewMode(mediaType, file.getFileExt()));

        if (file.getFolderId() != null) {
            vo.setFolderName(folderNameMap.get(file.getFolderId()));
        }

        if (!includeDisplayUrl) {
            return vo;
        }

        if (StringUtils.hasText(presignedUrl)) {
            vo.setDisplayUrl(presignedUrl);
            return vo;
        }

        if (!StringUtils.hasText(file.getObjectName())) {
            return vo;
        }

        if (FileMediaType.IMAGE.getCode().equals(file.getMediaType())) {
            String cachedUrl = displayUrlMap.get(file.getObjectName());
            if (StringUtils.hasText(cachedUrl)) {
                vo.setDisplayUrl(cachedUrl);
                return vo;
            }
            try {
                vo.setDisplayUrl(fileStorageService.getPresignedUrl(file.getObjectName()));
            } catch (Exception e) {
                vo.setDisplayUrl(fileStorageService.getPermanentUrl(file.getObjectName()));
            }
            return vo;
        }

        // 列表 batch 模式：非图片不生成 URL
        if (!displayUrlMap.isEmpty()) {
            return vo;
        }

        try {
            vo.setDisplayUrl(fileStorageService.getPresignedUrl(file.getObjectName()));
        } catch (Exception e) {
            vo.setDisplayUrl(fileStorageService.getPermanentUrl(file.getObjectName()));
        }
        return vo;
    }

    private String resolvePreviewMode(FileMediaType mediaType, String fileExt) {
        return switch (mediaType) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case AUDIO -> "audio";
            case DOCUMENT -> "pdf".equalsIgnoreCase(fileExt) ? "pdf" : "download";
            default -> "download";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int purgeExpiredRecycleFiles() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(purgeProperties.getRecycleRetentionDays());
        int batchSize = purgeProperties.getBatchSize();
        int totalPurged = 0;

        while (true) {
            List<SysFile> batch = baseMapper.selectExpiredRecycleFiles(deadline, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            totalPurged += physicallyDeleteRecycleFiles(batch);
            if (batch.size() < batchSize) {
                break;
            }
        }
        return totalPurged;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentDelete(List<Long> ids) {
        List<Long> distinctIds = normalizeIds(ids);
        AssertUtils.notEmpty(distinctIds, ErrorCode.PARAM_INVALID, "删除ID不能为空");

        List<SysFile> recycleFiles = listRecycleFiles(distinctIds);
        if (recycleFiles.size() != distinctIds.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分文件不在回收站或不存在");
        }
        assertCategoryAccess(recycleFiles);

        int deleted = physicallyDeleteRecycleFiles(recycleFiles);
        if (deleted != distinctIds.size()) {
            throw new BizException(ErrorCode.BIZ_ERROR, "部分文件永久删除失败（可能存储对象删除失败）");
        }
    }

    private int physicallyDeleteRecycleFiles(List<SysFile> files) {
        if (files.isEmpty()) {
            return 0;
        }
        List<Long> fileIds = files.stream().map(SysFile::getFileId).toList();
        Set<String> deletedObjects = new HashSet<>();
        List<Long> dbDeleteIds = new ArrayList<>();

        for (SysFile file : files) {
            String objectName = file.getObjectName();
            try {
                sysFileRefService.assertNoReference(file.getFileId());
            } catch (BizException e) {
                log.warn("跳过永久删除：文件仍被业务引用, fileId={}", file.getFileId());
                continue;
            }
            if (!StringUtils.hasText(objectName)) {
                dbDeleteIds.add(file.getFileId());
                continue;
            }
            if (baseMapper.countReferencesByObjectName(objectName, fileIds) > 0) {
                dbDeleteIds.add(file.getFileId());
                continue;
            }
            if (!deletedObjects.contains(objectName)) {
                try {
                    fileStorageService.deleteObject(objectName);
                    deletedObjects.add(objectName);
                } catch (Exception e) {
                    log.warn("物理删除 OSS 对象失败, fileId={}, objectName={}",
                            file.getFileId(), objectName, e);
                    continue;
                }
            }
            dbDeleteIds.add(file.getFileId());
        }

        if (dbDeleteIds.isEmpty()) {
            return 0;
        }
        sysFileRefService.deleteByFileIds(dbDeleteIds);
        return baseMapper.deletePhysicallyByIds(dbDeleteIds);
    }

    private void bindBizRefIfPresent(Long fileId, SysFileUploadDTO uploadDTO) {
        if (uploadDTO == null || fileId == null) {
            return;
        }
        if (StringUtils.hasText(uploadDTO.getBizType()) && StringUtils.hasText(uploadDTO.getBizId())) {
            sysFileRefService.bind(fileId, uploadDTO.getBizType(), uploadDTO.getBizId());
        }
    }

    private void applyAccessibleCategories(SysFileQueryDTO queryDTO) {
        List<String> accessible = fileCategoryAccessService.resolveAccessibleCategories();
        if (accessible.isEmpty()) {
            throw new BizException(ErrorCode.FORBIDDEN, "无可访问的文件分类");
        }
        queryDTO.setAccessibleCategories(accessible);
    }

    private void applyAccessibleCategories(SysFileRecycleQueryDTO queryDTO) {
        List<String> accessible = fileCategoryAccessService.resolveAccessibleCategories();
        if (accessible.isEmpty()) {
            throw new BizException(ErrorCode.FORBIDDEN, "无可访问的文件分类");
        }
        queryDTO.setAccessibleCategories(accessible);
    }

    private void assertCategoryAccess(List<SysFile> files) {
        for (SysFile file : files) {
            fileCategoryAccessService.requireAccess(file.getCategory());
        }
    }

    private SysFile requireActiveFile(Long fileId) {
        SysFile file = getActiveFile(fileId);
        AssertUtils.notNull(file, ErrorCode.NOT_FOUND, "文件不存在");
        fileCategoryAccessService.requireAccess(file.getCategory());
        return file;
    }

    private void assertUniqueFileName(Long folderId, String fileName, Long excludeFileId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getFolderId, folderId)
                .eq(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_NORMAL)
                .eq(SysFile::getFileName, fileName);
        if (excludeFileId != null) {
            wrapper.ne(SysFile::getFileId, excludeFileId);
        }
        if (count(wrapper) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "当前文件夹下已存在同名文件");
        }
    }

    private List<SysFile> listActiveFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return list(new LambdaQueryWrapper<SysFile>()
                .in(SysFile::getFileId, ids)
                .eq(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_NORMAL));
    }

    /** 查询回收站文件（自定义 SQL，避免逻辑删除插件与 del_flag=2 冲突）。 */
    private List<SysFile> listRecycleFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectRecycleByIds(ids);
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** DB 写入失败时尽力删除已上传的存储对象，避免孤儿文件。 */
    private void compensateUploadedObject(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return;
        }
        try {
            fileStorageService.deleteObject(objectName);
        } catch (Exception ignored) {
            // 补偿删除失败时不掩盖原始异常
        }
    }

    /** 查询未删除的有效文件（不再依赖 @TableLogic 自动过滤）。 */
    private SysFile getActiveFile(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getFileId, fileId)
                .eq(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_NORMAL));
    }
}
