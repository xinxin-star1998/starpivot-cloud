package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 砍价助力记录实体。
 * <p>
 * 对应数据库表 {@code sms_bargain_help}，同一会员对同一砍价记录仅可助力一次。
 * </p>
 */
@Data
@TableName("sms_bargain_help")
public class SmsBargainHelp {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    /** 助力会员 */
    private Long helperMemberId;

    /** 本次砍掉金额 */
    private BigDecimal cutAmount;

    private LocalDateTime createTime;
}
