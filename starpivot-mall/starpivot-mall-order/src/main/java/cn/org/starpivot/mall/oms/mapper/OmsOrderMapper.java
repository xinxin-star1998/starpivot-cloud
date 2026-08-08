package cn.org.starpivot.mall.oms.mapper;

import cn.org.starpivot.api.mall.order.dto.OrderSalesMonthAmountDto;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    IPage<OmsOrder> selectPageList(Page<OmsOrder> page, @Param("query") OmsOrderReqBo query);

    /**
     * 按月聚合已付款订单支付金额（payment_time 使用半开区间，便于走索引）。
     */
    @Select("SELECT DATE_FORMAT(payment_time, '%Y-%m') AS yearMonth, "
            + "COALESCE(SUM(pay_amount), 0) AS totalAmount "
            + "FROM oms_order "
            + "WHERE payment_time IS NOT NULL "
            + "AND payment_time >= #{startTime} "
            + "AND payment_time < #{endTimeExclusive} "
            + "AND status IN (1, 2, 3) "
            + "AND (delete_status IS NULL OR delete_status = 0) "
            + "GROUP BY DATE_FORMAT(payment_time, '%Y-%m') "
            + "ORDER BY yearMonth")
    List<OrderSalesMonthAmountDto> sumPayAmountByMonth(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTimeExclusive") LocalDateTime endTimeExclusive);
}
