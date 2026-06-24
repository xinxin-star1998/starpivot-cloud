package cn.org.starpivot.mall.ums.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_statistics_info")
public class UmsMemberStatisticsInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private BigDecimal consumeAmount;

    private BigDecimal couponAmount;

    private Integer orderCount;

    private Integer couponCount;

    private Integer commentCount;

    private Integer returnOrderCount;

    private Integer loginCount;

    private Integer attendCount;

    private Integer fansCount;

    private Integer collectProductCount;

    private Integer collectSubjectCount;

    private Integer collectCommentCount;

    private Integer inviteFriendCount;

}
