package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.storage.FileUploadVO;
import cn.org.starpivot.mall.pms.support.GoodsImageConstants;
import cn.org.starpivot.mall.pms.support.MallImageUploadSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商城通用图片上传（文件中心 GOODS 分类）。
 */
@Slf4j
@RestController
@RequestMapping("/mall/image")
@RequiredArgsConstructor
@Tag(name = "商城-图片", description = "商城素材图片上传（对接文件中心）")
public class MallImageController {

    private final MallImageUploadSupport mallImageUploadSupport;

    @Log(title = "商城图片上传", businessType = BusinessType.INSERT)
    @Operation(summary = "商城图片上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority("
            + "'mall:product:add', 'mall:product:edit',"
            + " 'mall:brand:add', 'mall:brand:edit',"
            + " 'mall:adv:add', 'mall:adv:edit',"
            + " 'mall:subject:add', 'mall:subject:edit'"
            + ")")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) throws Exception {
        FileUploadVO vo = mallImageUploadSupport.upload(
                file,
                GoodsImageConstants.ALLOWED_CONTENT_TYPES,
                GoodsImageConstants.MAX_SIZE_BYTES);
        log.info("商城图片上传成功，objectName={}", vo.getObjectName());
        return Result.success("上传成功", vo);
    }
}
