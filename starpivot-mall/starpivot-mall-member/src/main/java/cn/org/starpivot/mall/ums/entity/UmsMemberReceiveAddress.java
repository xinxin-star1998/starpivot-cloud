package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员收货地址实体。
 * <p>
 * 对应数据库表 {@code ums_member_receive_address}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member_receive_address")
public class UmsMemberReceiveAddress {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * 名称
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * post Code
     */
    private String postCode;

    /**
     * province
     */
    private String province;

    /**
     * city
     */
    private String city;

    /**
     * region
     */
    private String region;

    /**
     * detail Address
     */
    private String detailAddress;

    /**
     * areacode
     */
    private String areacode;

    /**
     * 状态
     */
    private Integer defaultStatus;

}
