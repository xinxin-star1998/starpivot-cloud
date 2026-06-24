package cn.org.starpivot.mall.pms.support;

import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.common.storage.FileCenterUploadHelper;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.FileUploadVO;
import cn.org.starpivot.common.storage.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商城图片上传：统一走文件中心 GOODS 分类路径（file/goods/...）。
 */
@Component
@RequiredArgsConstructor
public class MallImageUploadSupport {

    private final FileStorageService fileStorageService;
    private final FileCenterUploadHelper fileCenterUploadHelper;

    public FileUploadVO upload(MultipartFile file, String[] allowedTypes, long maxSizeBytes) throws Exception {
        fileStorageService.validateFile(file, allowedTypes, maxSizeBytes);
        UploadResult result = fileCenterUploadHelper.upload(
                file,
                FileCategory.GOODS.getObjectPathSegment(),
                FileCategory.GOODS.getDefaultFolderId());
        return FileUploadVO.from(result);
    }
}
