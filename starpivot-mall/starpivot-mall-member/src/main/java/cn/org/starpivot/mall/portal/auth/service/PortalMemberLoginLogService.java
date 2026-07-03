package cn.org.starpivot.mall.portal.auth.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * C 端会员登录日志写入（独立事务，避免只读登录事务吞掉审计记录）。
 */
public interface PortalMemberLoginLogService {

    void record(Long memberId, int loginType, HttpServletRequest request);
}
