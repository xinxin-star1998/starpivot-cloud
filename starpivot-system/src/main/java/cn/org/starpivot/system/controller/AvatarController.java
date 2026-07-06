package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像管理控制器。
 * <p>
 * 提供用户头像上传、预签名 URL 获取及删除功能，含本人/管理员权限校验。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /avatar}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入存储与权限服务</li>
 * </ul>
 *
 * @see FileStorageService
 * @see PermissionService
 */
@Slf4j
@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private static final String USER_MANAGE_PERMISSION = "system:user:list";

    private final FileStorageService fileStorageService;
    private final PermissionService permissionService;

    private boolean isSuperAdmin() {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        if (AppConstants.ADMIN_USER_ID.equals(currentUserId)) {
            return true;
        }
        return permissionService.hasRole(AppConstants.ADMIN_ROLE_KEY);
    }

    private boolean hasUserManagePermission() {
        return permissionService.hasPermission(USER_MANAGE_PERMISSION);
    }

    private boolean hasPermissionToViewUser(String targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        final Long targetId;
        try {
            targetId = Long.parseLong(targetUserId);
        } catch (NumberFormatException e) {
            return false;
        }
        if (currentUserId.equals(targetId)) {
            return true;
        }
        return isSuperAdmin() || hasUserManagePermission();
    }

    private boolean hasPermissionToModifyUser(String targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        final Long targetId;
        try {
            targetId = Long.parseLong(targetUserId);
        } catch (NumberFormatException e) {
            return false;
        }
        if (currentUserId.equals(targetId)) {
            return true;
        }
        return isSuperAdmin();
    }

    private String extractUserIdFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        if (filePath.startsWith("avatar/")) {
            String fileName = filePath.substring(7);
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                return fileName.substring(0, dotIndex);
            }
            return fileName;
        }
        if (filePath.startsWith("user/")) {
            String[] parts = filePath.split("/");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return null;
    }

    /**
     * 上传用户头像至对象存储。
     * <p>
     * 限制文件大小 5MB、仅允许图片类型；本人或超级管理员可修改目标用户头像。
     * 标注 {@link Log} 记录操作审计。
     * </p>
     *
     * @param file            头像图片文件
     * @param userId          目标用户 ID
     * @param usePresignedUrl 是否同时返回预签名访问 URL
     * @return 头像访问地址及是否预签名标识
     */
    @Log(title = "上传用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "usePresignedUrl", defaultValue = "false") boolean usePresignedUrl) {
        try {
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BizException(ErrorCode.PARAM_INVALID, "文件大小不能超过5MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/")
                    && !contentType.startsWith("application/octet-stream"))) {
                throw new BizException(ErrorCode.PARAM_INVALID, "只允许上传图片文件");
            }
            if (!hasPermissionToModifyUser(userId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "只能上传自己的头像");
            }

            Map<String, String> data = new HashMap<>();
            if (usePresignedUrl) {
                Map<String, String> uploadResult = fileStorageService.uploadAvatarWithFullInfo(file, userId);
                data.put("avatarUrl", uploadResult.get("avatarUrl"));
                data.put("presignedUrl", uploadResult.get("presignedUrl"));
                data.put("isPresigned", "true");
            } else {
                String avatarUrl = fileStorageService.uploadAvatarWithUrl(file, userId);
                data.put("avatarUrl", avatarUrl);
                data.put("isPresigned", "false");
            }
            return Result.success("上传成功", data);
        } catch (BizException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("上传头像参数错误, userId={}", userId, e);
            return Result.error("参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("上传头像失败, userId={}", userId, e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 根据存储路径获取头像预签名访问 URL。
     *
     * @param filePath 对象存储中的头像路径
     * @return 预签名 URL
     */
    @GetMapping("/presigned-url")
    public Result<Map<String, String>> getPresignedUrl(@RequestParam("filePath") String filePath) {
        try {
            String userId = extractUserIdFromPath(filePath);
            if (userId == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "无效的文件路径格式");
            }
            if (!hasPermissionToViewUser(userId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权查看该用户的头像");
            }
            String objectName = StorageObjectPathUtils.normalizeToObjectName(filePath);
            String presignedUrl = fileStorageService.getPresignedUrl(objectName);
            Map<String, String> data = new HashMap<>();
            data.put("presignedUrl", presignedUrl);
            return Result.success("获取成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 删除指定用户的头像文件。
     * <p>{@link PreAuthorize} 要求已认证；业务层校验本人或超级管理员权限。</p>
     *
     * @param userId 目标用户 ID
     * @return 操作结果
     */
    @Log(title = "删除用户头像", businessType = BusinessType.DELETE)
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/removeAvatar")
    public Result<?> delete(@RequestParam("userId") String userId) {
        try {
            if (!hasPermissionToModifyUser(userId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "只能删除自己的头像");
            }
            fileStorageService.deleteAvatar(userId);
            return Result.success("删除成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
