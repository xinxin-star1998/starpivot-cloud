package cn.org.starpivot.file.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.annotation.NoResponseWrapper;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.StoragePathValidator;
import cn.org.starpivot.common.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用文件上传 REST 控制器。
 * <p>
 * 提供富文本编辑器图片上传与 OSS 预签名 URL 查询，路径前缀 {@code /common/upload}。
 * <ul>
 *   <li>{@link RestController} — 声明 REST API，返回值默认 JSON 序列化</li>
 *   <li>{@link RequestMapping} — 类级路径映射 {@code /common/upload}</li>
 *   <li>{@link RequiredArgsConstructor} — 注入 {@link FileStorageService}</li>
 *   <li>{@link Slf4j} — 记录上传异常与非法路径访问</li>
 * </ul>
 *
 * @see FileStorageService
 * @see StoragePathValidator
 */
@Slf4j
@RestController
@RequestMapping("/common/upload")
@RequiredArgsConstructor
public class CommonUploadController {

    private final FileStorageService fileStorageService;

    /**
     * WangEditor 富文本图片上传。
     * <p>
     * 成功返回 {@code { errno: 0, data: { url, alt, href } }}；失败返回 {@code { errno: 1, message }}。
     * 使用 {@link NoResponseWrapper} 跳过统一 {@link Result} 包装以兼容编辑器协议。
     *
     * @param file 上传的图片文件，表单字段名 {@code file}
     * @return WangEditor 约定格式的 Map，非 {@link Result} 包装
     */
    @Log(title = "富文本图片上传", businessType = cn.org.starpivot.common.enums.BusinessType.OTHER)
    @NoResponseWrapper
    @PostMapping("/wangeditor")
    public Map<String, Object> uploadWangEditor(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return errorBody("文件不能为空");
        }
        try {
            String url = fileStorageService.uploadEditorImageWithUrl(file);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("url", url);
            data.put("alt", "");
            data.put("href", "");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("errno", 0);
            body.put("data", data);
            return body;
        } catch (IllegalArgumentException e) {
            log.warn("富文本图片参数错误: {}", e.getMessage());
            return errorBody(e.getMessage());
        } catch (Exception e) {
            log.error("富文本图片上传失败", e);
            String msg = e.getMessage();
            return errorBody(msg != null && !msg.isEmpty() ? msg : "上传失败");
        }
    }

    /**
     * 获取单个 OSS 对象的临时访问（预签名）URL。
     *
     * @param objectName OSS 对象路径，须通过 {@link StoragePathValidator#isAllowedPresignedPath} 校验
     * @return 成功时 {@link Result} 含 {@code objectName} 与 {@code url}；路径非法或为空时返回错误
     * @throws Exception {@link FileStorageService#getPresignedUrl} 生成失败时抛出
     */
    @GetMapping("/presigned-url")
    public Result<Map<String, String>> getPresignedUrl(@RequestParam("objectName") String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            return Result.error("对象路径不能为空");
        }
        if (!StoragePathValidator.isAllowedPresignedPath(objectName)) {
            log.warn("尝试获取未授权路径的预签名URL: {}", objectName);
            return Result.error("无权访问该路径的临时访问链接");
        }
        String presignedUrl = fileStorageService.getPresignedUrl(objectName);
        return Result.success("获取成功", Map.of(
                "objectName", objectName,
                "url", presignedUrl
        ));
    }

    /**
     * 批量获取 OSS 对象预签名 URL；跳过空路径或未授权路径，不中断整批请求。
     *
     * @param objectNames OSS 对象路径列表，可为 {@code null} 或空
     * @return 成功时 {@link Result} 含 {@code objectName → url} 映射；空列表返回空 Map
     * @throws Exception 单个对象生成预签名 URL 失败时抛出
     */
    @PostMapping("/presigned-urls")
    public Result<Map<String, String>> getPresignedUrls(@RequestBody List<String> objectNames) throws Exception {
        if (objectNames == null || objectNames.isEmpty()) {
            return Result.success(Map.of());
        }
        Map<String, String> urls = new LinkedHashMap<>();
        for (String objectName : objectNames) {
            if (!StringUtils.hasText(objectName) || !StoragePathValidator.isAllowedPresignedPath(objectName)) {
                continue;
            }
            urls.put(objectName, fileStorageService.getPresignedUrl(objectName));
        }
        return Result.success(urls);
    }

    /**
     * 构造 WangEditor 错误响应体。
     *
     * @param message 错误描述，{@code null} 时使用默认文案
     * @return {@code { errno: 1, message }}
     */
    private static Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errno", 1);
        body.put("message", message != null ? message : "上传失败");
        return body;
    }
}
