package cn.org.starpivot.file.service;

import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.file.config.FileCategoryAccessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Category 级数据权限校验。
 */
@Service
@RequiredArgsConstructor
public class FileCategoryAccessService {

    private final FileCategoryAccessProperties properties;

    public void requireAccess(String categoryCode) {
        if (!canAccess(categoryCode)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问该业务分类文件");
        }
    }

    public boolean canAccess(String categoryCode) {
        if (!properties.isEnabled() || categoryCode == null || categoryCode.isBlank()) {
            return true;
        }
        if (hasAllCategoryAccess()) {
            return true;
        }
        String normalized = categoryCode.toUpperCase(Locale.ROOT);
        if (!properties.getRestrictedCategories().contains(normalized)) {
            return true;
        }
        return SecurityContextUtils.hasAuthority(FileCategory.of(normalized).getAccessPermission());
    }

    public List<String> resolveAccessibleCategories() {
        return Arrays.stream(FileCategory.values())
                .map(FileCategory::getCode)
                .filter(this::canAccess)
                .toList();
    }

    private boolean hasAllCategoryAccess() {
        Long userId = SecurityContextUtils.getUserId();
        if (AppConstants.ADMIN_USER_ID.equals(userId)) {
            return true;
        }
        LoginUser loginUser = SecurityContextUtils.getLoginUser();
        if (loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().contains(AppConstants.ADMIN_ROLE_KEY)) {
            return true;
        }
        return SecurityContextUtils.hasAuthority(properties.getAllCategoriesPermission());
    }
}
