package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Returnaudit请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class ReturnAuditBo {

    /**
     * 主键 ID
     */
    /**
     * 主键 ID
     */
    @NotNull(message = "退货申请ID不能为空")
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 状态
     */
    /**
     * status
     */
    @NotNull(message = "审核状态不能为空")
    /**
     * status
     */
    private Integer status;

    /**
     * handle Note
     */
    private String handleNote;

    /**
     * handle Man
     */
    private String handleMan;
}
