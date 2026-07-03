package cn.org.starpivot.mall.ums.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会员保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberSaveBo {

    /**
     * 主键 ID
     */
    /**
     * 主键 ID
     */
    @NotNull(message = "会员ID不能为空")
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * nickname
     */
    private String nickname;
    /**
     * 状态
     */
    private Integer status;
    /**
     * integration
     */
    private Integer integration;
    /**
     * growth
     */
    private Integer growth;
}
