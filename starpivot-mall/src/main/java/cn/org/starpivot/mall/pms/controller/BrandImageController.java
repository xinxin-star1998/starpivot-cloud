package cn.org.starpivot.mall.pms.controller;



import cn.org.starpivot.common.annotation.Log;

import cn.org.starpivot.common.enums.BusinessType;

import cn.org.starpivot.common.domain.Result;

import cn.org.starpivot.common.storage.FileUploadVO;

import cn.org.starpivot.mall.pms.support.BrandImageConstants;

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

 * 商城品牌 Logo 上传（文件中心 GOODS 分类）。

 */

@Slf4j

@RestController

@RequestMapping("/mall/brand/image")

@RequiredArgsConstructor

@Tag(name = "商城-品牌图片", description = "品牌 Logo 上传")

public class BrandImageController {



    private final MallImageUploadSupport mallImageUploadSupport;



    /**

     * 上传品牌 Logo

     *

     * @param file    图片文件

     * @param brandId 品牌 ID（保留参数兼容前端）

     */

    @Log(title = "品牌Logo上传", businessType = BusinessType.INSERT)

    @Operation(summary = "品牌Logo上传")

    @PostMapping("/upload")

    @PreAuthorize("hasAnyAuthority('mall:brand:add', 'mall:brand:edit')")

    public Result<FileUploadVO> upload(

            @RequestParam("file") MultipartFile file,

            @RequestParam(value = "brandId", required = false) Long brandId) throws Exception {

        FileUploadVO vo = mallImageUploadSupport.upload(

                file,

                BrandImageConstants.ALLOWED_CONTENT_TYPES,

                BrandImageConstants.MAX_SIZE_BYTES);

        log.info("品牌Logo上传成功，brandId={}, objectName={}", brandId, vo.getObjectName());

        return Result.success("上传成功", vo);

    }

}


