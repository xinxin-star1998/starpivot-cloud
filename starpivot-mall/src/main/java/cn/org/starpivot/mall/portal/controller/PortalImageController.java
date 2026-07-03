package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.PresignedUrlsBo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.storage.FileUploadVO;
import cn.org.starpivot.mall.pms.support.GoodsImageConstants;
import cn.org.starpivot.mall.pms.support.MallImageDisplaySupport;
import cn.org.starpivot.mall.pms.support.MallImageUploadSupport;
import cn.org.starpivot.mall.portal.PortalConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/image")
@RequiredArgsConstructor
@Tag(name = "C端-图片", description = "评价晒图等会员上传")
public class PortalImageController {

    private final MallImageUploadSupport mallImageUploadSupport;
    private final MallImageDisplaySupport mallImageDisplaySupport;

    @Operation(summary = "评价晒图上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) throws Exception {
        FileUploadVO vo = mallImageUploadSupport.upload(
                file,
                GoodsImageConstants.ALLOWED_CONTENT_TYPES,
                GoodsImageConstants.MAX_SIZE_BYTES);
        return Result.success("上传成功", vo);
    }

    @Operation(summary = "批量获取商城展示图预签名 URL")
    @PostMapping("/presigned-urls")
    public Result<Map<String, String>> presignedUrls(@Valid @RequestBody PresignedUrlsBo bo) {
        List<String> objectNames = bo.getObjectNames() == null ? List.of() : bo.getObjectNames();
        return Result.success(mallImageDisplaySupport.resolveDisplayUrls(objectNames));
    }
}
