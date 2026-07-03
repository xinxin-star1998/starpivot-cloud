package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.api.member.dto.MemberAddressDto;
import cn.org.starpivot.api.member.dto.MemberDto;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderItemVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;
import cn.org.starpivot.mall.portal.service.PortalOrderPricingResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单实体组装、VO 转换与操作流水写入。
 */
@Component
@RequiredArgsConstructor
public class PortalOrderAssembler {

    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;

    public String generateOrderSn() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return PortalConstants.ORDER_SN_PREFIX + timePart + randomPart;
    }

    public OmsOrder buildOrder(
            MemberDto member,
            MemberAddressDto address,
            PortalOrderSubmitBo bo,
            String orderSn,
            LocalDateTime now,
            PortalOrderPricingResult pricing) {
        OmsOrder order = new OmsOrder();
        order.setMemberId(member.getId());
        order.setMemberUsername(member.getUsername());
        order.setOrderSn(orderSn);
        order.setCreateTime(now);
        order.setModifyTime(now);
        order.setStatus(PortalConstants.ORDER_STATUS_UNPAID);
        order.setDeleteStatus(0);
        order.setConfirmStatus(0);
        order.setSourceType(0);
        order.setPayType(bo.getPayType() == null ? PortalConstants.PAY_TYPE_ALIPAY : bo.getPayType());
        order.setTotalAmount(pricing.getOriginalAmount());
        order.setPayAmount(pricing.getPayAmount());
        order.setFreightAmount(pricing.getFreightAmount());
        order.setPromotionAmount(pricing.getPromotionAmount());
        order.setIntegrationAmount(pricing.getIntegrationAmount());
        order.setUseIntegration(pricing.getUseIntegration());
        order.setCouponAmount(pricing.getCouponAmount() == null ? BigDecimal.ZERO : pricing.getCouponAmount());
        order.setCouponId(pricing.getCouponId());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setNote(bo.getNote());
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        return order;
    }

    public OmsOrderItem buildOrderItem(
            String orderSn, int quantity, PortalOrderSkuSupport.SkuSnapshot snapshot, BigDecimal linePromotion) {
        var dto = snapshot.snapshot();
        OmsOrderItem item = new OmsOrderItem();
        item.setOrderSn(orderSn);
        item.setSpuId(dto.getSpuId());
        item.setSpuName(dto.getSpuName());
        item.setCategoryId(dto.getCatalogId());
        item.setSpuBrand(dto.getBrandName());
        item.setSkuId(dto.getSkuId());
        item.setSkuName(StringUtils.hasText(dto.getSkuTitle()) ? dto.getSkuTitle() : dto.getSkuName());
        item.setSkuPrice(snapshot.unitPrice() != null ? snapshot.unitPrice() : dto.getPrice());
        item.setSkuQuantity(quantity);
        item.setSkuAttrsVals(dto.getAttrs());
        item.setPromotionAmount(linePromotion == null ? BigDecimal.ZERO : linePromotion);
        item.setCouponAmount(BigDecimal.ZERO);
        item.setIntegrationAmount(BigDecimal.ZERO);
        item.setRealAmount(snapshot.lineAmount());
        if (StringUtils.hasText(dto.getSkuDefaultImg())) {
            item.setSkuPic(StorageObjectPathUtils.normalizeToObjectName(dto.getSkuDefaultImg()));
            item.setSpuPic(item.getSkuPic());
        }
        return item;
    }

    public void saveOperateHistory(Long orderId, Integer status, String operator, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }

    public PortalOrderVo toOrderVoWithoutItems(OmsOrder order) {
        PortalOrderVo vo = new PortalOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    public OmsOrderItemVo toItemVo(OmsOrderItem item) {
        OmsOrderItemVo vo = new OmsOrderItemVo();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    public Map<Long, List<OmsOrderItem>> loadItemsByOrderIds(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().in(OmsOrderItem::getOrderId, orderIds));
        return items.stream().collect(Collectors.groupingBy(OmsOrderItem::getOrderId));
    }
}
