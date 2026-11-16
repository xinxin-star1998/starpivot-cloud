package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.system.constants.PasswordModifyReason;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.vo.PasswordModifyHint;
import cn.org.starpivot.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 密码策略校验（读取 sys_config）。
 * <p>
 * 依次评估初始密码策略与密码有效期策略，命中任一即要求登录后修改密码。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final SysConfigService sysConfigService;
    private final SecurityUtils securityUtils;

    /**
     * 评估登录用户是否需要修改密码
     *
     * @param user 已认证用户
     */
    public PasswordModifyHint evaluate(SysUser user) {
        if (user == null) {
            return PasswordModifyHint.none();
        }

        PasswordModifyHint initPasswordHint = evaluateInitPasswordModify(user);
        if (initPasswordHint.isRequired()) {
            return initPasswordHint;
        }

        return evaluatePasswordExpire(user);
    }

    private PasswordModifyHint evaluateInitPasswordModify(SysUser user) {
        if (sysConfigService.getInitPasswordModifyPolicy() != 1) {
            return PasswordModifyHint.none();
        }
        if (!StringUtils.hasText(user.getPassword())) {
            return PasswordModifyHint.none();
        }
        String initPassword = sysConfigService.getInitPassword();
        if (StringUtils.hasText(initPassword) && securityUtils.matchesPassword(initPassword, user.getPassword())) {
            return PasswordModifyHint.required(PasswordModifyReason.INIT_PASSWORD);
        }
        return PasswordModifyHint.none();
    }

    private PasswordModifyHint evaluatePasswordExpire(SysUser user) {
        int validateDays = sysConfigService.getPasswordValidateDays();
        if (validateDays <= 0 || user.getPwdUpdateDate() == null) {
            return PasswordModifyHint.none();
        }
        LocalDateTime expireAt = user.getPwdUpdateDate().plusDays(validateDays);
        if (expireAt.isBefore(LocalDateTime.now())) {
            return PasswordModifyHint.required(PasswordModifyReason.PASSWORD_EXPIRED);
        }
        return PasswordModifyHint.none();
    }
}
