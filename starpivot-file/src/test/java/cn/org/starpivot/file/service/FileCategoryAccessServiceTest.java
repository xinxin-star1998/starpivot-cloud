package cn.org.starpivot.file.service;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.file.config.FileCategoryAccessProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileCategoryAccessServiceTest {

    @Mock
    private FileCategoryAccessProperties properties;

    @InjectMocks
    private FileCategoryAccessService fileCategoryAccessService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.isEnabled()).thenReturn(true);
        lenient().when(properties.getRestrictedCategories()).thenReturn(List.of("HR", "FINANCE"));
        lenient().when(properties.getAllCategoriesPermission()).thenReturn("file:category:all");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void canAccess_openCategoryWithoutExtraPermission() {
        mockUserAuthorities("file:resource:query");

        assertTrue(fileCategoryAccessService.canAccess("SYSTEM"));
    }

    @Test
    void canAccess_restrictedCategoryRequiresPermission() {
        mockUserAuthorities("file:resource:query");

        assertFalse(fileCategoryAccessService.canAccess("HR"));
    }

    @Test
    void canAccess_restrictedCategoryWithPermission() {
        mockUserAuthorities("file:resource:query", "file:category:HR");

        assertTrue(fileCategoryAccessService.canAccess("HR"));
    }

    @Test
    void canAccess_adminBypassesRestriction() {
        LoginUser admin = LoginUser.builder()
                .userId(AppConstants.ADMIN_USER_ID)
                .username("admin")
                .roles(List.of("admin"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of()));

        assertTrue(fileCategoryAccessService.canAccess("HR"));
    }

    @Test
    void requireAccess_throwsWhenDenied() {
        mockUserAuthorities("file:resource:query");

        BizException ex = assertThrows(BizException.class,
                () -> fileCategoryAccessService.requireAccess("FINANCE"));
        assertEquals(ErrorCode.FORBIDDEN, ex.getCode());
    }

    private void mockUserAuthorities(String... authorities) {
        LoginUser user = LoginUser.builder().userId(2L).username("user").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        java.util.Arrays.stream(authorities)
                                .map(auth -> (org.springframework.security.core.GrantedAuthority) () -> auth)
                                .toList()));
    }
}
