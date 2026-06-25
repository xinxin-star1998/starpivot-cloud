package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * Purchasemerge请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseMergeBo {

    /** 目标采购单 ID，为空则新建 */
    /**
     * Purchase ID
     */
    private Long purchaseId;

    /**
     * items
     */
    /**
     * items
     */
    @NotEmpty(message = "合并的采购需求不能为空")
    /**
     * items
     */
    private List<Long> items;
}
