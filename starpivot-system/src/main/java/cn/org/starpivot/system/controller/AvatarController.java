package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像管理控制器
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
        return hasUserManagePermission();
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
            String presignedUrl = fileStorageService.getPresignedUrl(filePath);
            Map<String, String> data = new HashMap<>();
            data.put("presignedUrl", presignedUrl);
            return Result.success("获取成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @Log(title = "删除用户头像", businessType = BusinessType.DELETE)
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete")
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
