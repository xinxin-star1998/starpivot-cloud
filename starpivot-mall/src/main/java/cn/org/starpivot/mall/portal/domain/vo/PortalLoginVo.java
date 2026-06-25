package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;


/**
 * Login视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalLoginVo {

    /**
     * token
     */
    private String token;

    /**
     * member
     */
    private PortalMemberVo member;
}
