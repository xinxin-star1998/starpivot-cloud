package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.io.Serializable;

@Service
public class AccountLockService {

    private final SysUserService sysUserService;

    public AccountLockService(@Lazy SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    public UnlockResult unlockUserByUserId(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return new UnlockResult(false, "用户不存在");
        }
        return new UnlockResult(true, "账户未被锁定，无需解锁");
    }

    public boolean isAccountLocked(String username) {
        return false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnlockResult implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private boolean success;
        private String message;
    }
}
