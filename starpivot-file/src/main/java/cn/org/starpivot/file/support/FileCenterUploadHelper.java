package cn.org.starpivot.file.support;

import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件中心上传路径生成与存储封装。
 */
@Component
@RequiredArgsConstructor
public class FileCenterUploadHelper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageService fileStorageService;

    public UploadResult upload(MultipartFile file, FileCategory category, Long folderId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String suffix = resolveSuffix(file.getOriginalFilename());
        String objectName = buildObjectName(category, folderId, suffix);
        fileStorageService.uploadFileInternal(file, objectName);
        return UploadResult.builder()
                .objectName(objectName)
                .permanentUrl(fileStorageService.getPermanentUrl(objectName))
                .presignedUrl(fileStorageService.getPresignedUrl(objectName))
                .build();
    }

    public String buildObjectName(FileCategory category, Long folderId, String suffix) {
        return String.format("%s%d/%s/%s%s",
                category.getObjectPathSegment(),
                folderId,
                LocalDate.now().format(DATE_FMT),
                UUID.randomUUID(),
                suffix);
    }

    private String resolveSuffix(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
