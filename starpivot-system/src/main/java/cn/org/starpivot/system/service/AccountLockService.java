package cn.org.starpivot.system.service;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 账号锁定服务
 * 负责管理用户登录失败次数、账号锁定状态和解锁逻辑
 */
@Slf4j
@Service
public class AccountLockService {

    private final SysUserService sysUserService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 最大登录失败次数
     */
    @Value("${starpivot.security.login.max-fail-count:5}")
    private int maxFailCount;

    /**
     * 锁定时间（分钟）
     */
    @Value("${starpivot.security.login.lock-duration:15}")
    private int lockDuration;

    public AccountLockService(@Lazy SysUserService sysUserService, RedisTemplate<String, Object> redisTemplate) {
        this.sysUserService = sysUserService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean recordLoginFailure(String username) {
        String failCountKey = CacheConstants.pwdErrCountKey(username);
        String lockKey = CacheConstants.accountLockKey(username);

        // 检查是否已被锁定
        if (isAccountLocked(username)) {
            return true;
        }

        // 递增失败次数
        Long failCount = redisTemplate.opsForValue().increment(failCountKey);
        if (failCount == null) {
            failCount = 1L;
            redisTemplate.opsForValue().set(failCountKey, failCount, lockDuration, TimeUnit.MINUTES);
        }

        // 如果达到最大失败次数，锁定账号
        if (failCount >= maxFailCount) {
            log.warn("Account locked due to too many failed login attempts: {}", username);
            redisTemplate.opsForValue().set(lockKey, LocalDateTime.now().toString(), lockDuration, TimeUnit.MINUTES);
            redisTemplate.delete(failCountKey);
            return true;
        }

        return false;
    }

    /**
     * 检查账号是否被锁定
     *
     * @param username 用户名
     * @return true-已锁定，false-未锁定
     */
    public boolean isAccountLocked(String username) {
        String lockKey = CacheConstants.accountLockKey(username);
        Boolean exists = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 获取账号剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余秒数，未锁定返回0
     */
    public long getRemainingLockSeconds(String username) {
        String lockKey = CacheConstants.accountLockKey(username);
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 登录成功后重置失败计数
     *
     * @param username 用户名
     */
    public void resetLoginFailureCount(String username) {
        String failCountKey = CacheConstants.pwdErrCountKey(username);
        redisTemplate.delete(failCountKey);
    }

    /**
     * 根据用户ID解锁用户
     *
     * @param userId 用户ID
     * @return 解锁结果
     */
    public UnlockResult unlockUserByUserId(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return new UnlockResult(false, "用户不存在");
        }
        return unlockUserByUsername(user.getUserName());
    }

    /**
     * 根据用户名解锁用户
     *
     * @param username 用户名
     * @return 解锁结果
     */
    public UnlockResult unlockUserByUsername(String username) {
        String lockKey = CacheConstants.accountLockKey(username);
        String failCountKey = CacheConstants.pwdErrCountKey(username);

        Boolean isLocked = redisTemplate.hasKey(lockKey);
        if (Boolean.FALSE.equals(isLocked)) {
            return new UnlockResult(true, "账户未被锁定，无需解锁");
        }

        redisTemplate.delete(lockKey);
        redisTemplate.delete(failCountKey);
        log.info("Account unlocked by admin: {}", username);
        return new UnlockResult(true, "解锁成功");
    }

    /**
     * 获取登录失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getLoginFailCount(String username) {
        String failCountKey = CacheConstants.pwdErrCountKey(username);
        Object count = redisTemplate.opsForValue().get(failCountKey);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    /**
     * 获取最大登录失败次数
     */
    public int getMaxFailCount() {
        return maxFailCount;
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