package cn.org.starpivot.mall.portal.subscribe.service;

import cn.org.starpivot.api.member.dto.MemberSubscribeNotifyRequest;
import cn.org.starpivot.mall.portal.subscribe.domain.vo.PortalSubscribeTemplatesVo;

public interface PortalMiniSubscribeService {

    PortalSubscribeTemplatesVo templates();

    /** 按场景发送；未配置或失败仅记日志，不抛给调用方业务主流程 */
    void notifyQuietly(MemberSubscribeNotifyRequest request);
}
