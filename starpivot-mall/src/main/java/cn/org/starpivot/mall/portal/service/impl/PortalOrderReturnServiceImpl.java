package cn.org.starpivot.mall.portal.service.impl;


import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderReturnApplyBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderReturnItemBo;
import cn.org.starpivot.mall.portal.service.PortalOrderReturnService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



@Service

@RequiredArgsConstructor

public class PortalOrderReturnServiceImpl implements PortalOrderReturnService {



    private final OmsOrderMapper omsOrderMapper;

    private final OmsOrderItemMapper omsOrderItemMapper;

    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;



    @Override

    @Transactional(rollbackFor = Exception.class)

    public List<Long> applyReturn(Long memberId, PortalOrderReturnApplyBo bo) {

        List<PortalOrderReturnItemBo> items = resolveItems(bo);

        if (items.isEmpty()) {

            throw new BizException("退货商品不能为空");

        }

        OmsOrder order = requireReturnableOrder(memberId, bo.getOrderId());

        List<Long> applyIds = new ArrayList<>();

        for (PortalOrderReturnItemBo item : items) {

            applyIds.add(applySingleItem(order, item, bo.getReason(), bo.getDescription()));

        }

        return applyIds;

    }



    private List<PortalOrderReturnItemBo> resolveItems(PortalOrderReturnApplyBo bo) {

        if (!CollectionUtils.isEmpty(bo.getItems())) {

            Map<Long, Integer> qtyBySku = new LinkedHashMap<>();

            for (PortalOrderReturnItemBo item : bo.getItems()) {

                if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {

                    continue;

                }

                qtyBySku.merge(item.getSkuId(), item.getQuantity(), Integer::sum);

            }

            List<PortalOrderReturnItemBo> merged = new ArrayList<>();

            qtyBySku.forEach((skuId, qty) -> {

                PortalOrderReturnItemBo row = new PortalOrderReturnItemBo();

                row.setSkuId(skuId);

                row.setQuantity(qty);

                merged.add(row);

            });

            return merged;

        }

        if (bo.getSkuId() != null && bo.getQuantity() != null && bo.getQuantity() > 0) {

            PortalOrderReturnItemBo single = new PortalOrderReturnItemBo();

            single.setSkuId(bo.getSkuId());

            single.setQuantity(bo.getQuantity());

            return List.of(single);

        }

        return List.of();

    }



    private OmsOrder requireReturnableOrder(Long memberId, Long orderId) {

        OmsOrder order = omsOrderMapper.selectById(orderId);

        if (order == null

                || Integer.valueOf(1).equals(order.getDeleteStatus())

                || !memberId.equals(order.getMemberId())) {

            throw new BizException("订单不存在");

        }

        if (!Integer.valueOf(PortalConstants.ORDER_STATUS_DELIVERED).equals(order.getStatus())

                && !Integer.valueOf(PortalConstants.ORDER_STATUS_COMPLETED).equals(order.getStatus())) {

            throw new BizException("仅已发货或已完成订单可申请退货");

        }

        return order;

    }



    private Long applySingleItem(

            OmsOrder order, PortalOrderReturnItemBo itemBo, String reason, String description) {

        OmsOrderItem item = omsOrderItemMapper.selectOne(

                Wrappers.<OmsOrderItem>lambdaQuery()

                        .eq(OmsOrderItem::getOrderId, order.getId())

                        .eq(OmsOrderItem::getSkuId, itemBo.getSkuId())

                        .last("LIMIT 1"));

        if (item == null) {

            throw new BizException("订单中不存在该商品：" + itemBo.getSkuId());

        }

        int qty = itemBo.getQuantity();

        if (qty <= 0 || qty > item.getSkuQuantity()) {

            throw new BizException("退货数量无效：" + item.getSkuName());

        }

        long pending = omsOrderReturnApplyMapper.selectCount(

                Wrappers.<OmsOrderReturnApply>lambdaQuery()

                        .eq(OmsOrderReturnApply::getOrderId, order.getId())

                        .eq(OmsOrderReturnApply::getSkuId, itemBo.getSkuId())

                        .in(

                                OmsOrderReturnApply::getStatus,

                                OmsConstants.RETURN_STATUS_PENDING,

                                OmsConstants.RETURN_STATUS_RETURNING));

        if (pending > 0) {

            throw new BizException("该商品已有进行中的退货申请：" + item.getSkuName());

        }



        BigDecimal unit = item.getRealAmount() != null && item.getSkuQuantity() != null && item.getSkuQuantity() > 0

                ? item.getRealAmount().divide(BigDecimal.valueOf(item.getSkuQuantity()), 2, java.math.RoundingMode.HALF_UP)

                : item.getSkuPrice();

        BigDecimal returnAmount = unit.multiply(BigDecimal.valueOf(qty));



        OmsOrderReturnApply apply = new OmsOrderReturnApply();

        apply.setOrderId(order.getId());

        apply.setOrderSn(order.getOrderSn());

        apply.setSkuId(itemBo.getSkuId());

        apply.setMemberUsername(order.getMemberUsername());

        apply.setReturnAmount(returnAmount);

        apply.setReturnName(order.getReceiverName());

        apply.setReturnPhone(order.getReceiverPhone());

        apply.setStatus(OmsConstants.RETURN_STATUS_PENDING);

        apply.setAuditStatus(MallAuditStatus.DRAFT);

        apply.setCreateTime(LocalDateTime.now());

        apply.setSkuName(item.getSkuName());

        apply.setSkuBrand(item.getSpuBrand());

        apply.setSkuImg(item.getSkuPic());

        apply.setSkuAttrsVals(item.getSkuAttrsVals());

        apply.setSkuCount(qty);

        apply.setSkuPrice(item.getSkuPrice());

        apply.setSkuRealPrice(unit);

        apply.setReason(reason);

        apply.setDescription(description);

        omsOrderReturnApplyMapper.insert(apply);

        return apply.getId();

    }

}

