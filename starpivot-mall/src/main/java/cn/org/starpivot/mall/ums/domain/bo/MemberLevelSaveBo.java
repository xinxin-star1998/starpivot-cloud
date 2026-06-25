package cn.org.starpivot.mall.ums.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 会员等级保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberLevelSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "等级名称不能为空")
    @Size(max = 64, message = "等级名称长度不能超过64")
    /**
     * name
     */
    private String name;

    /**
     * growth Point
     */
    private Integer growthPoint;
    /**
     * 状态
     */
    private Integer defaultStatus;
    /**
     * free Freight Point
     */
    private BigDecimal freeFreightPoint;
    /**
     * comment Growth Point
     */
    private Integer commentGrowthPoint;
    /**
     * privilege Free Freight
     */
    private Integer privilegeFreeFreight;
    /**
     * privilege Member Price
     */
    private Integer privilegeMemberPrice;
    /**
     * privilege Birthday
     */
    private Integer privilegeBirthday;

    /**
     * note
     */
    /**
     * 备注
     */
    @Size(max = 512, message = "备注长度不能超过512")
    /**
     * 备注
     */
    private String note;
}
