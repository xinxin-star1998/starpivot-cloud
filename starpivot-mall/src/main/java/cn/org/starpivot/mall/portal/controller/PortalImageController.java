package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.storage.FileUploadVO;
import cn.org.starpivot.mall.pms.support.GoodsImageConstants;
import cn.org.starpivot.mall.pms.support.MallImageUploadSupport;
import cn.org.starpivot.mall.portal.PortalConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/portal/image")
@RequiredArgsConstructor
@Tag(name = "C端-图片", description = "评价晒图等会员上传")
public class PortalImageController {

    private final MallImageUploadSupport mallImageUploadSupport;

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
}
