package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberReqBo extends PageReqBo {

    /**
     * 用户名
     */
    private String username;
    /**
     * mobile
     */
    private String mobile;
    /**
     * 状态
     */
    private Integer status;
}
