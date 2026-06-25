package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

/**
 * 会员视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalMemberVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * nickname
     */
    private String nickname;

    /**
     * mobile
     */
    private String mobile;

    /**
     * header
     */
    private String header;

    /**
     * integration
     */
    private Integer integration;

    /**
     * growth
     */
    private Integer growth;

    /**
     * Level ID
     */
    private Long levelId;
}
