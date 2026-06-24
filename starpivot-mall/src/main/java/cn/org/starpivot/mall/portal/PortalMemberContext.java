package cn.org.starpivot.mall.portal;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;

/**
 * 从安全上下文获取当前 C 端会员 ID。
 */
public final class PortalMemberContext {

    private PortalMemberContext() {
    }

    public static Long requireMemberId() {
        Long memberId = SecurityContextUtils.getUserId();
        if (memberId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return memberId;
    }
}
