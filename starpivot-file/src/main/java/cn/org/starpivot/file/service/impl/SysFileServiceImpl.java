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
import cn.org.starpivot.file.domain.bo.SysFileVO;
import cn.org.starpivot.file.domain.dto.SysFileQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileRecycleQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileUploadDTO;
import cn.org.starpivot.file.domain.entity.SysFile;
import cn.org.starpivot.file.domain.entity.SysFileFolder;
import cn.org.starpivot.file.mapper.SysFileFolderMapper;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.service.ISysFileService;
import cn.org.starpivot.file.support.FileMediaTypeResolver;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private final SysFileMapper sysFileMapper;
    private final SysFileFolderMapper sysFileFolderMapper;
    private final FileCenterUploadHelper fileCenterUploadHelper;
    private final FileMediaTypeResolver fileMediaTypeResolver;
    private final FileStorageService fileStorageService;
    private final FileCenterProperties fileCenterProperties;

    @Value("${oss.enabled:true}")
    private boolean ossEnabled;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFileVO upload(MultipartFile file, Long folderId, SysFileUploadDTO uploadDTO) {
        AssertUtils.notNull(folderId, ErrorCode.PARAM_INVALID, "文件夹ID不能为空");
        SysFileFolder folder = sysFileFolderMapper.selectById(folderId);
        AssertUtils.notNull(folder, ErrorCode.NOT_FOUND, "文件夹不存在");

        FileCategory category = FileCategory.of(folder.getCategory());
        FileMediaType mediaType = fileMediaTypeResolver.resolve(file);
        validateFileSize(file, mediaType);

        UploadResult uploadResult;
        try {
            uploadResult = fileCenterUploadHelper.upload(
                    file, category.getObjectPathSegment(), folderId);
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }

        SysFile entity = new SysFile();
        entity.setFolderId(folderId);
        entity.setCategory(category.getCode());
        entity.setMediaType(mediaType.getCode());
        entity.setFileName(file.getOriginalFilename());
        entity.setFileExt(fileMediaTypeResolver.extractExtension(file.getOriginalFilename()));
        entity.setContentType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setObjectName(uploadResult.getObjectName());
        entity.setStorageProvider(ossEnabled ? "OSS" : "LOCAL");
        entity.setDelFlag(FileBizConstants.DEL_FLAG_NORMAL);
        entity.setCreateBy(SecurityContextUtils.getUsername());
        entity.setCreateTime(LocalDateTime.now());

        if (uploadDTO != null) {
            entity.setBizType(uploadDTO.getBizType());
            entity.setBizId(uploadDTO.getBizId());
            entity.setRemark(uploadDTO.getRemark());
        }

        save(entity);
        return toVO(entity, uploadResult.getPresignedUrl());
    }

    @Override
    public PageResponse<SysFileVO> pageList(SysFileQueryDTO queryDTO) {
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysFile> result = sysFileMapper.selectPageList(page, queryDTO);
        return toPageResponse(result, true);
    }

    @Override
    public SysFileVO getDetail(Long fileId) {
        SysFile file = getActiveFile(fileId);
        AssertUtils.notNull(file, ErrorCode.NOT_FOUND, "文件不存在");
        return toVO(file, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDelete(List<Long> ids) {
        AssertUtils.notEmpty(ids, ErrorCode.PARAM_INVALID, "删除ID不能为空");
        String username = SecurityContextUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            SysFile file = getActiveFile(id);
            if (file == null) {
                continue;
            }
            update(new LambdaUpdateWrapper<SysFile>()
                    .eq(SysFile::getFileId, id)
                    .set(SysFile::getDelFlag, FileBizConstants.DEL_FLAG_RECYCLE)
                    .set(SysFile::getDeleteBy, username)
                    .set(SysFile::getDeleteTime, now));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(List<Long> ids) {
        AssertUtils.notEmpty(ids, ErrorCode.PARAM_INVALID, "恢复ID不能为空");
        sysFileMapper.restoreByIds(ids, SecurityContextUtils.getUsername());
    }

    @Override
    public PageResponse<SysFileVO> recyclePage(SysFileRecycleQueryDTO queryDTO) {
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysFile> result = sysFileMapper.selectRecyclePageList(page, queryDTO);
        return toPageResponse(result, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToFolder(List<Long> ids, Long targetFolderId) {
        AssertUtils.notEmpty(ids, ErrorCode.PARAM_INVALID, "文件ID不能为空");
        AssertUtils.notNull(targetFolderId, ErrorCode.PARAM_INVALID, "目标文件夹不能为空");

        SysFileFolder targetFolder = sysFileFolderMapper.selectById(targetFolderId);
        AssertUtils.notNull(targetFolder, ErrorCode.NOT_FOUND, "目标文件夹不存在");

        String category = targetFolder.getCategory();
        String username = SecurityContextUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();
        int moved = 0;

        for (Long id : ids) {
            SysFile file = getActiveFile(id);
            if (file == null) {
                continue;
            }
            if (!FileBizConstants.DEL_FLAG_NORMAL.equals(file.getDelFlag())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "回收站中的文件不可迁移");
            }
            if (targetFolderId.equals(file.getFolderId())) {
                continue;
            }
            update(new LambdaUpdateWrapper<SysFile>()
                    .eq(SysFile::getFileId, id)
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

        SysFile file = getActiveFile(fileId);
        AssertUtils.notNull(file, ErrorCode.NOT_FOUND, "文件不存在");

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
        SysFile file = getActiveFile(fileId);
        AssertUtils.notNull(file, ErrorCode.NOT_FOUND, "文件不存在");
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

        PageResponse<SysFileVO> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setPageCount(page.getPages());
        response.setRows(records.stream()
                .map(file -> buildVO(file, null, folderNameMap, displayUrlMap, includeDisplayUrl))
                .collect(Collectors.toList()));
        return response;
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

    private SysFileVO toVO(SysFile file, String presignedUrl) {
        Map<Long, String> folderNameMap = Collections.emptyMap();
        if (file.getFolderId() != null) {
            SysFileFolder folder = sysFileFolderMapper.selectById(file.getFolderId());
            if (folder != null) {
                folderNameMap = Map.of(folder.getFolderId(), folder.getFolderName());
            }
        }
        return buildVO(file, presignedUrl, folderNameMap, Collections.emptyMap(), true);
    }

    private SysFileVO buildVO(
            SysFile file,
            String presignedUrl,
            Map<Long, String> folderNameMap,
            Map<String, String> displayUrlMap,
            boolean includeDisplayUrl) {
        SysFileVO vo = new SysFileVO();
        BeanUtils.copyProperties(file, vo);

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
