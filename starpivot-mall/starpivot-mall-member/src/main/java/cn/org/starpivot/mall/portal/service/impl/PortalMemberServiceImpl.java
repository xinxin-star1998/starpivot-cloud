package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.mall.portal.auth.service.PortalAuthService;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberProfileBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCenterVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalPasswordLoginBo;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.service.PortalMemberService;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.sms.entity.SmsCouponHistory;
import cn.org.starpivot.mall.sms.mapper.SmsCouponHistoryMapper;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSpu;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSpuMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.portal.auth.service.PortalTokenService;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class PortalMemberServiceImpl implements PortalMemberService {

    private static final int COUPON_UNUSED = 0;

    private final PortalAuthService portalAuthService;
    private final PortalTokenService portalTokenService;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final UmsMemberCollectSpuMapper collectSpuMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final SmsCouponHistoryMapper smsCouponHistoryMapper;
    private final PmsSpuCommentMapper pmsSpuCommentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(PortalMemberRegisterBo bo) {
        portalAuthService.registerWithAuth(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo login(PortalMemberLoginBo bo) {
        PortalPasswordLoginBo passwordBo = new PortalPasswordLoginBo();
        passwordBo.setAccount(bo.getAccount());
        passwordBo.setPassword(bo.getPassword());
        return portalAuthService.loginByPassword(passwordBo, currentRequest());
    }

    @Override
    @Transactional(readOnly = true)
    public PortalMemberVo getCurrentMember() {
        return enrichMemberVo(loadMember(PortalMemberContext.requireMemberId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PortalMemberCenterVo getCenter() {
        Long memberId = PortalMemberContext.requireMemberId();
        UmsMember member = loadMember(memberId);

        PortalMemberCenterVo center = new PortalMemberCenterVo();
        center.setMember(enrichMemberVo(member));
        center.setLevelName(resolveLevelName(member.getLevelId()));

        Long collectCount = collectSpuMapper.selectCount(
                Wrappers.<UmsMemberCollectSpu>lambdaQuery().eq(UmsMemberCollectSpu::getMemberId, memberId));
        center.setCollectCount(collectCount != null ? collectCount.intValue() : 0);

        Long orderCount = omsOrderMapper.selectCount(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getMemberId, memberId));
        center.setOrderCount(orderCount != null ? orderCount.intValue() : 0);

        Long couponCount = smsCouponHistoryMapper.selectCount(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getUseType, COUPON_UNUSED));
        center.setCouponCount(couponCount != null ? couponCount.intValue() : 0);

        Long commentCount = pmsSpuCommentMapper.selectCount(
                Wrappers.<PmsSpuComment>lambdaQuery().eq(PmsSpuComment::getMemberId, memberId));
        center.setCommentCount(commentCount != null ? commentCount.intValue() : 0);
        center.setPendingReviewCount(0);
        return center;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalMemberVo updateProfile(PortalMemberProfileBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        UmsMember member = loadMember(memberId);

        if (StringUtils.hasText(bo.getNickname())) {
            member.setNickname(bo.getNickname().trim());
        }
        if (bo.getHeader() != null) {
            member.setHeader(bo.getHeader().trim());
        }
        if (bo.getGender() != null) {
            member.setGender(bo.getGender());
        }
        if (bo.getSign() != null) {
            member.setSign(bo.getSign().trim());
        }
        umsMemberMapper.updateById(member);
        return enrichMemberVo(member);
    }

    private UmsMember loadMember(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "会员不存在");
        }
        return member;
    }

    private PortalMemberVo enrichMemberVo(UmsMember member) {
        PortalMemberVo vo = portalTokenService.toMemberVo(member);
        vo.setLevelName(resolveLevelName(member.getLevelId()));
        vo.setGender(member.getGender());
        vo.setEmail(member.getEmail());
        vo.setSign(member.getSign());
        return vo;
    }

    private String resolveLevelName(Long levelId) {
        if (levelId == null) {
            return null;
        }
        UmsMemberLevel level = umsMemberLevelMapper.selectById(levelId);
        return level != null ? level.getName() : null;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
