package cn.org.starpivot.mall.portal.subscribe.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortalSubscribeTemplatesVo {

    /** 是否启用订阅消息 */
    private boolean enabled;

    /** 可向用户申请的模板 ID 列表 */
    private List<String> templateIds;

    private String paySuccessTemplateId;
    private String deliverTemplateId;
    private String pendingReviewTemplateId;
}
