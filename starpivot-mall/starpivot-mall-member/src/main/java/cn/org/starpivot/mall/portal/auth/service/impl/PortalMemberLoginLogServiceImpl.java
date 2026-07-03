package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.util.LogUtils;
import cn.org.starpivot.mall.portal.auth.mapper.UmsMemberLoginLogMapper;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberLoginLogService;
import cn.org.starpivot.mall.ums.entity.UmsMemberLoginLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PortalMemberLoginLogServiceImpl implements PortalMemberLoginLogService {

    private final UmsMemberLoginLogMapper loginLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(Long memberId, int loginType, HttpServletRequest request) {
        UmsMemberLoginLog log = new UmsMemberLoginLog();
        log.setMemberId(memberId);
        log.setLoginType(loginType);
        log.setCreateTime(LocalDateTime.now());
        if (request != null) {
            log.setIp(LogUtils.getClientIp(request));
        }
        loginLogMapper.insert(log);
    }
}
