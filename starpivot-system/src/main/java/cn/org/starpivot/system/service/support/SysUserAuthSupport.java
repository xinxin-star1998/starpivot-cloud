package cn.org.starpivot.system.service.support;

import cn.org.starpivot.api.system.dto.SysRoleInfoDto;
import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户认证查询与 DTO 转换。
 */
@Component
@RequiredArgsConstructor
public class SysUserAuthSupport {

    private final SysUserMapper sysUserMapper;
    private final SecurityUtils securityUtils;

    public SysUser findActiveUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username)
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL));
    }

    public SysUserAuthDto getAuthByUsername(String username) {
        SysUser user = findActiveUserByUsername(username);
        return user == null ? null : toAuthDto(user);
    }

    public SysUserAuthDto verifyPassword(String username, String rawPassword) {
        SysUser user = findActiveUserByUsername(username);
        if (user == null || !StringUtils.hasText(rawPassword)) {
            return null;
        }
        if (!securityUtils.matchesPassword(rawPassword, user.getPassword())) {
            return null;
        }
        return toAuthDto(user);
    }

    public SysUserAuthDto toAuthDto(SysUser user) {
        List<SysRole> roleEntities = sysUserMapper.getRolesByUserId(user.getUserId());
        // getRolesByUserId 对 admin 角色会带出停用项；认证用 roleKey 必须与旧逻辑一致，仅启用角色进入 JWT
        List<SysRole> activeRoles = roleEntities.stream()
                .filter(r -> AppConstants.Status.NORMAL.equals(r.getStatus()))
                .toList();
        List<String> roleKeys = activeRoles.stream()
                .map(SysRole::getRoleKey)
                .toList();
        List<SysRoleInfoDto> roleList = activeRoles.stream()
                .map(r -> SysRoleInfoDto.builder()
                        .roleId(r.getRoleId())
                        .roleName(r.getRoleName())
                        .roleKey(r.getRoleKey())
                        .roleSort(r.getRoleSort())
                        .status(r.getStatus())
                        .build())
                .toList();
        return SysUserAuthDto.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .nickName(user.getNickName())
                .status(user.getStatus())
                .roles(roleKeys)
                .roleList(roleList)
                .avatar(user.getAvatar())
                .build();
    }
}
