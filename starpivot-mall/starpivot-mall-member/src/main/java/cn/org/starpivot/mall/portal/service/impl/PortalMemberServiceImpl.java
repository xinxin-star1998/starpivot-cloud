package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.OrderFeignSupport;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.common.PromotionFeignSupport;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalPasswordLoginBo;
import cn.org.starpivot.mall.portal.auth.service.PortalAuthService;
import cn.org.starpivot.mall.portal.auth.service.PortalTokenService;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberProfileBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCenterVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.service.PortalMemberService;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSpu;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSpuMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
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

    private final PortalAuthService portalAuthService;
    private final PortalTokenService portalTokenService;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final UmsMemberCollectSpuMapper collectSpuMapper;
    private final OrderFeignSupport orderFeignSupport;
    private final PromotionFeignSupport promotionFeignSupport;
    private final ProductFeignSupport productFeignSupport;

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

        center.setOrderCount(orderFeignSupport.countByMember(memberId));
        center.setCouponCount(promotionFeignSupport.countUnusedCoupons(memberId));
        center.setCommentCount(productFeignSupport.countCommentsByMember(memberId));
        center.setPendingReviewCount(productFeignSupport.countPendingReviews(memberId));
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
