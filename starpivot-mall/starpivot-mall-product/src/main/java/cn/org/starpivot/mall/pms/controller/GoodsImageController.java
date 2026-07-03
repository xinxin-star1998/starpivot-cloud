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
 * 商城-商品图片控制器。
 * <p>
 * 商品 SPU/SKU 图片上传。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/goods/image}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-商品图片」</li>
 * </ul>
 *
 * @see MallImageUploadSupport
 */

@Slf4j
@RestController
@RequestMapping("/mall/goods/image")
@RequiredArgsConstructor
@Tag(name = "商城-商品图片", description = "商品 SPU/SKU 图片上传")
public class GoodsImageController {

    private final MallImageUploadSupport mallImageUploadSupport;

    /**
     * 商品图片上传。
     *
     * @param file 上传的 Excel 或图片文件
     * @param goodsId 主键 ID
     * @return 业务数据
     * @throws Exception 文件解析或上传异常
     */
    @Log(title = "商品图片上传", businessType = BusinessType.INSERT)
    @Operation(summary = "商品图片上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('mall:product:add', 'mall:product:edit')")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "goodsId", required = false) Long goodsId) throws Exception {
        FileUploadVO vo = mallImageUploadSupport.upload(
                file,
                GoodsImageConstants.ALLOWED_CONTENT_TYPES,
                GoodsImageConstants.MAX_SIZE_BYTES);
        log.info("商品图片上传成功，goodsId={}, objectName={}", goodsId, vo.getObjectName());
        return Result.success("上传成功", vo);
    }
}
