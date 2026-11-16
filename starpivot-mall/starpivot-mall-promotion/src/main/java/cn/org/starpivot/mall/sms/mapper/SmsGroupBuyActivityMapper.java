package cn.org.starpivot.mall.sms.mapper;

import cn.org.starpivot.mall.sms.entity.SmsGroupBuyActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SmsGroupBuyActivityMapper extends BaseMapper<SmsGroupBuyActivity> {

    /**
     * 条件扣减活动库存，库存不足时返回 0。
     *
     * @param id    活动 ID
     * @param count 扣减件数
     * @return 影响行数
     */
    @Update("UPDATE sms_group_buy_activity SET stock_count = stock_count - #{count} "
            + "WHERE id = #{id} AND stock_count >= #{count}")
    int decreaseStock(@Param("id") Long id, @Param("count") int count);

    /**
     * 回补活动库存。
     *
     * @param id    活动 ID
     * @param count 回补件数
     * @return 影响行数
     */
    @Update("UPDATE sms_group_buy_activity SET stock_count = stock_count + #{count} WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("count") int count);
}
