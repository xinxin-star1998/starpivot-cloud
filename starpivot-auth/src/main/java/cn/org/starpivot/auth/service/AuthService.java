package cn.org.starpivot.auth.service;

import cn.org.starpivot.api.system.SysConfigClient;
import cn.org.starpivot.api.system.SysUserClient;
import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.api.system.dto.RegisterUserRequest;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.api.system.dto.SysMenuDto;
import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.api.system.dto.VerifyPasswordRequest;
import cn.org.starpivot.auth.domain.LoginRequest;
import cn.org.starpivot.auth.domain.LoginResponse;
import cn.org.starpivot.auth.domain.RefreshRequest;
import cn.org.starpivot.auth.domain.RegisterRequest;
import cn.org.starpivot.auth.domain.RegisterResponse;
import cn.org.starpivot.auth.domain.UserInfoResponse;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.util.LogUtils;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 认证业务服务类。
 * <p>
 * 封装登录、注册、令牌刷新、登出及用户信息查询等核心认证逻辑，
 * 通过 Feign 客户端调用 system 服务完成用户校验与日志记录。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — Lombok 生成 {@code log} 日志字段</li>
 *   <li>{@link Service} — 注册为 Spring 业务服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 Feign 客户端及依赖服务</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserClient sysUserClient;
    private final SysConfigClient sysConfigClient;
    private final LoginLogRecordService loginLogRecordService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 用户登录：校验凭据、签发 JWT 与刷新令牌，并记录登录日志。
     *
     * @param request 登录请求，含用户名与密码
     * @return 登录响应，含访问令牌、刷新令牌及用户基本信息
     * @throws BusinessException 凭据错误或用户已停用时抛出 401
     */
    public LoginResponse login(LoginRequest request) {
        HttpServletRequest httpRequest = currentRequest();
        String ip = httpRequest != null ? LogUtils.getClientIp(httpRequest) : "";
        String browser = httpRequest != null ? LogUtils.getBrowser(httpRequest) : "";
        String os = httpRequest != null ? LogUtils.getOs(httpRequest) : "";
        String loginLocation = LogUtils.getLoginLocation(ip);
        
        try {
            SysUserAuthDto userDto = verifyUserCredentials(request.getUsername(), request.getPassword());

            if (!AppConstants.Status.NORMAL.equals(userDto.getStatus())) {
                recordLoginLog(request.getUsername(), ip, httpRequest, "1", "用户已停用");
                throw new BusinessException(401, "用户名或密码错误");
            }

            LoginUser user = LoginUser.builder()
                    .userId(userDto.getUserId())
                    .username(userDto.getUsername())
                    .roles(userDto.getRoles())
                    .build();

            String token = JwtUtils.createToken(user, jwtProperties);
            String refreshToken = refreshTokenService.createRefreshToken(userDto.getUserId(), ip, browser, os, loginLocation);
            recordLoginLog(request.getUsername(), ip, httpRequest, "0", AppConstants.LOGIN_SUCCESS);

            return LoginResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .username(userDto.getUsername())
                    .nickname(userDto.getNickName() != null ? userDto.getNickName() : userDto.getUsername())
                    .expiresIn(jwtProperties.getExpire() / 1000)
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("登录失败, username={}", request.getUsername(), ex);
            recordLoginLog(request.getUsername(), ip, httpRequest, "1", "登录异常");
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    /**
     * 使用刷新令牌换取新的访问令牌与刷新令牌（令牌轮换）。
     *
     * @param request 刷新请求，含用户名与刷新令牌
     * @return 新的登录响应
     * @throws BusinessException 刷新令牌无效或已过期时抛出 401
     */
    public LoginResponse refreshToken(RefreshRequest request) {
        SysUserAuthDto userDto = loadUserForLogin(request.getUsername());
        if (!refreshTokenService.validate(userDto.getUserId(), request.getRefreshToken())) {
            throw new BusinessException(401, "刷新令牌无效或已过期，请重新登录");
        }
        refreshTokenService.revoke(userDto.getUserId());

        LoginUser user = LoginUser.builder()
                .userId(userDto.getUserId())
                .username(userDto.getUsername())
                .roles(userDto.getRoles())
                .build();
        String token = JwtUtils.createToken(user, jwtProperties);
        String refreshToken = refreshTokenService.createRefreshToken(userDto.getUserId());

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(userDto.getUsername())
                .nickname(userDto.getNickName() != null ? userDto.getNickName() : userDto.getUsername())
                .expiresIn(jwtProperties.getExpire() / 1000)
                .build();
    }

    /**
     * 用户自助注册，需系统配置已开启注册功能。
     *
     * @param request 注册请求，含用户名与密码
     * @return 注册响应，含新用户 ID 及用户名
     * @throws BusinessException 未开放注册或远程注册失败时抛出
     */
    public RegisterResponse register(RegisterRequest request) {
        Result<Boolean> enabledResult = sysConfigClient.isRegisterEnabled();
        if (enabledResult == null || enabledResult.getData() == null || !enabledResult.getData()) {
            throw new BusinessException(403, "当前未开放用户注册");
        }
        RegisterUserRequest registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUsername(request.getUsername().trim());
        registerUserRequest.setPassword(request.getPassword());
        Result<RegisterUserResponse> result = sysUserClient.registerUser(registerUserRequest);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException(500, result != null ? result.getMessage() : "注册失败");
        }
        RegisterUserResponse data = result.getData();
        return RegisterResponse.builder()
                .userId(data.getUserId())
                .username(data.getUsername())
                .nickName(data.getNickName())
                .build();
    }

    /**
     * 查询系统是否已开启用户注册功能。
     *
     * @return 已开启返回 {@code true}，否则返回 {@code false}
     */
    public boolean isRegisterEnabled() {
        Result<Boolean> result = sysConfigClient.isRegisterEnabled();
        return result != null && Boolean.TRUE.equals(result.getData());
    }

    /**
     * 退出登录：将访问令牌加入黑名单并撤销对应刷新令牌。
     *
     * @param token JWT 访问令牌，可为空（空则跳过处理）
     */
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenBlacklistService.add(token, jwtProperties.getExpire());
            try {
                LoginUser loginUser = JwtUtils.toLoginUser(JwtUtils.parseToken(token, jwtProperties.getSecret()));
                refreshTokenService.revoke(loginUser.getUserId());
            } catch (Exception ignored) {
                // ignore invalid token on logout
            }
        }
    }

    /**
     * 根据访问令牌获取当前用户的详细信息、角色及菜单权限。
     *
     * @param token JWT 访问令牌
     * @return 用户信息响应
     * @throws BusinessException 令牌缺失、已失效或用户不存在时抛出 401
     */
    public UserInfoResponse getUserInfo(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(401, "未授权，请先登录");
        }
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new BusinessException(401, "令牌已失效，请重新登录");
        }
        LoginUser loginUser = JwtUtils.toLoginUser(JwtUtils.parseToken(token, jwtProperties.getSecret()));
        SysUserAuthDto userDto = loadUserForLogin(loginUser.getUsername());

        List<Object> permissions = Collections.emptyList();
        try {
            Result<List<SysMenuDto>> menuResult = sysUserClient.getUserMenus(loginUser.getUserId());
            if (menuResult != null && menuResult.getData() != null) {
                permissions = menuResult.getData().stream()
                        .map(Object.class::cast)
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.warn("获取用户菜单权限失败, userId={}", loginUser.getUserId(), ex);
        }

        return UserInfoResponse.builder()
                .user(UserInfoResponse.UserVo.builder()
                        .userId(userDto.getUserId())
                        .username(userDto.getUsername())
                        .nickName(userDto.getNickName() != null ? userDto.getNickName() : userDto.getUsername())
                        .avatar(userDto.getAvatar() != null ? userDto.getAvatar() : "")
                        .email("")
                        .phoneNumber("")
                        .sex(0)
                        .status(userDto.getStatus())
                        .createTime("")
                        .build())
                .roles(buildRoles(userDto.getRoles()))
                .permissions(permissions)
                .build();
    }

    private List<UserInfoResponse.RoleVo> buildRoles(List<String> roleKeys) {
        if (roleKeys == null || roleKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return IntStream.range(0, roleKeys.size())
                .mapToObj(i -> UserInfoResponse.RoleVo.builder()
                        .roleId((long) (i + 1))
                        .roleName(roleKeys.get(i))
                        .roleKey(roleKeys.get(i))
                        .roleSort(i + 1)
                        .status(AppConstants.Status.NORMAL)
                        .createTime("")
                        .build())
                .toList();
    }

    private SysUserAuthDto loadUserForLogin(String username) {
        Result<SysUserAuthDto> result = sysUserClient.getByUsername(username);
        if (result == null || result.getData() == null || !result.isSuccess()) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return result.getData();
    }

    private SysUserAuthDto verifyUserCredentials(String username, String password) {
        VerifyPasswordRequest verifyRequest = new VerifyPasswordRequest();
        verifyRequest.setUsername(username);
        verifyRequest.setPassword(password);
        Result<SysUserAuthDto> result = sysUserClient.verifyPassword(verifyRequest);
        if (result == null || result.getData() == null || !result.isSuccess()) {
            HttpServletRequest httpRequest = currentRequest();
            String ip = httpRequest != null ? LogUtils.getClientIp(httpRequest) : "";
            recordLoginLog(username, ip, httpRequest, "1", "密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }
        return result.getData();
    }

    private void recordLoginLog(String username, String ip, HttpServletRequest request, String status, String msg) {
        try {
            LoginLogDto dto = new LoginLogDto();
            dto.setUserName(username);
            dto.setIpaddr(ip);
            dto.setLoginLocation(LogUtils.getLoginLocation(ip));
            dto.setBrowser(LogUtils.getBrowser(request));
            dto.setOs(LogUtils.getOs(request));
            dto.setStatus(status);
            dto.setMsg(msg);
            loginLogRecordService.record(dto);
        } catch (Exception ignored) {
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
