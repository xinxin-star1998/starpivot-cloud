package cn.org.starpivot.mall.sms.mapper;

import cn.org.starpivot.mall.sms.entity.SmsCoupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SmsCouponMapper extends BaseMapper<SmsCoupon> {

    /**
     * 行锁读取优惠券，串行化同一券的领取，避免 perLimit 并发超领。
     */
    @Select("SELECT * FROM sms_coupon WHERE id = #{couponId} FOR UPDATE")
    SmsCoupon selectByIdForUpdate(@Param("couponId") Long couponId);

    /**
     * 原子递增领取数量并校验库存。
     * <p>
     * 将 receive_count + 1 与库存校验合并为单条 SQL，
     * 避免并发读取 receiveCount 导致超发。
     * </p>
     *
     * @param couponId 优惠券 ID
     * @return 受影响行数（1 表示成功，0 表示库存不足）
     */
    @Update("UPDATE sms_coupon SET receive_count = IFNULL(receive_count, 0) + 1 " +
            "WHERE id = #{couponId} " +
            "AND (publish_count IS NULL OR publish_count <= 0 " +
            "OR IFNULL(receive_count, 0) < publish_count)")
    int incrementReceiveCount(@Param("couponId") Long couponId);

    /**
     * 原子递增使用数量。
     *
     * @param couponId 优惠券 ID
     * @return 受影响行数
     */
    @Update("UPDATE sms_coupon SET use_count = IFNULL(use_count, 0) + 1 WHERE id = #{couponId}")
    int incrementUseCount(@Param("couponId") Long couponId);
}
