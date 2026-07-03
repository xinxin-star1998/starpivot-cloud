package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.config.MallSecurityProperties;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.service.impl.OmsOrderLifecycleService;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.*;
import cn.org.starpivot.mall.portal.domain.vo.*;
import cn.org.starpivot.mall.portal.service.*;
import cn.org.starpivot.mall.portal.service.support.PortalOrderAssembler;
import cn.org.starpivot.mall.portal.service.support.PortalOrderItemResolver;
import cn.org.starpivot.mall.portal.service.support.PortalOrderSkuSupport;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类。
 *
 * @see PortalOrderService
 */
@Service
@RequiredArgsConstructor
public class PortalOrderServiceImpl implements PortalOrderService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberReceiveAddressMapper addressMapper;
    private final PortalAddressService portalAddressService;
    private final MallSecurityProperties mallSecurityProperties;
    private final PortalStockLockService portalStockLockService;
    private final PortalOrderPayService portalOrderPayService;
    private final PortalOrderPricingService portalOrderPricingService;
    private final OmsOrderLifecycleService omsOrderLifecycleService;
    private final PortalOrderReturnService portalOrderReturnService;
    private final PortalCouponService portalCouponService;
    private final PortalMemberIntegrationService portalMemberIntegrationService;
    private final PortalOrderTokenService portalOrderTokenService;
    private final PortalCommentService portalCommentService;
    private final PortalSeckillStockService portalSeckillStockService;
    private final PortalCartService portalCartService;
    private final PortalOrderSkuSupport portalOrderSkuSupport;
    private final PortalOrderItemResolver portalOrderItemResolver;
    private final PortalOrderAssembler portalOrderAssembler;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> statusCounts(Long memberId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int status : List.of(0, 1, 2, 3, 4)) {
            Long count = omsOrderMapper.selectCount(
                    Wrappers.<OmsOrder>lambdaQuery()
                            .eq(OmsOrder::getMemberId, memberId)
                            .eq(OmsOrder::getDeleteStatus, 0)
                            .eq(OmsOrder::getStatus, status));
            counts.put(String.valueOf(status), count != null ? count.intValue() : 0);
        }
        counts.put("review", portalCommentService.countPendingReviews(memberId));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalOrderPriceTrialVo priceTrial(Long memberId, PortalOrderPriceTrialBo bo) {
        List<PortalOrderItemBo> orderItems = portalOrderItemResolver.resolveTrialItems(memberId, bo);
        PortalOrderPriceTrialBo trialBo = new PortalOrderPriceTrialBo();
        trialBo.setItems(orderItems);
        trialBo.setCouponHistoryId(bo.getCouponHistoryId());
        trialBo.setUseIntegration(bo.getUseIntegration());
        PortalOrderPriceTrialVo vo = portalOrderPricingService.trial(memberId, trialBo);
        enrichTrialLineTitles(vo, portalOrderSkuSupport.loadSkuBaseMap(orderItems));
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalOrderSubmitTokenVo issueSubmitToken(Long memberId) {
        PortalOrderSubmitTokenVo vo = new PortalOrderSubmitTokenVo();
        vo.setOrderToken(portalOrderTokenService.issueSubmitToken(memberId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalOrderSubmitVo submit(Long memberId, PortalOrderSubmitBo bo) {
        portalOrderTokenService.consumeSubmitToken(memberId, bo.getOrderToken());
        UmsMember member = requireMember(memberId);
        UmsMemberReceiveAddress address = requireMemberAddress(memberId, bo.getAddressId());
        List<PortalOrderItemBo> orderItems = portalOrderItemResolver.resolveOrderItems(memberId, bo);
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BizException("下单商品不能为空");
        }

        PortalOrderPricingResult pricing = portalOrderPricingService.calculate(
                memberId, orderItems, bo.getCouponHistoryId(), bo.getUseIntegration(), true);

        String orderSn = portalOrderAssembler.generateOrderSn();
        LocalDateTime now = LocalDateTime.now();
        List<OmsOrderItem> itemEntities = new ArrayList<>();
        Map<Long, Integer> qtyBySku = aggregateQuantityBySku(orderItems);
        Map<Long, PortalOrderSkuSupport.SkuBase> skuBaseMap = portalOrderSkuSupport.loadSkuBaseMap(orderItems);
        List<PortalOrderPricingResult.PricedLine> pricedLines = pricing.getLines();

        for (int i = 0; i < orderItems.size(); i++) {
            PortalOrderItemBo itemBo = orderItems.get(i);
            PortalOrderSkuSupport.SkuBase base = skuBaseMap.get(itemBo.getSkuId());
            if (base == null) {
                throw new BizException("SKU不存在：" + itemBo.getSkuId());
            }
            PortalOrderPricingResult.PricedLine priced = i < pricedLines.size() ? pricedLines.get(i) : null;
            BigDecimal unitPrice = priced != null ? priced.getUnitPrice() : base.sku().getPrice();
            BigDecimal lineAmount = priced != null
                    ? priced.getLineAmount()
                    : unitPrice.multiply(BigDecimal.valueOf(itemBo.getQuantity()));
            BigDecimal linePromotion = priced != null ? priced.getPromotionAmount() : BigDecimal.ZERO;
            PortalOrderSkuSupport.SkuSnapshot snapshot =
                    portalOrderSkuSupport.toSnapshot(base, itemBo.getQuantity(), unitPrice, lineAmount);
            itemEntities.add(portalOrderAssembler.buildOrderItem(orderSn, itemBo.getQuantity(), snapshot, linePromotion));
        }

        portalStockLockService.lockForOrder(orderSn, qtyBySku);
        Long createdOrderId = null;
        try {
            OmsOrder order = portalOrderAssembler.buildOrder(member, address, bo, orderSn, now, pricing);
            omsOrderMapper.insert(order);
            createdOrderId = order.getId();
            for (OmsOrderItem item : itemEntities) {
                item.setOrderId(order.getId());
                omsOrderItemMapper.insert(item);
            }
            portalCouponService.lockToOrder(bo.getCouponHistoryId(), memberId, order.getId(), orderSn);
            portalMemberIntegrationService.deductForOrder(order);
            portalOrderAssembler.saveOperateHistory(
                    order.getId(), PortalConstants.ORDER_STATUS_UNPAID, member.getUsername(), "提交订单");

            if (Boolean.TRUE.equals(bo.getUseCart())) {
                List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).toList();
                portalCartService.clearChecked(memberId, skuIds);
            }

            PortalOrderSubmitVo vo = new PortalOrderSubmitVo();
            vo.setOrderId(order.getId());
            vo.setOrderSn(orderSn);
            vo.setStatus(PortalConstants.ORDER_STATUS_UNPAID);
            return vo;
        } catch (RuntimeException ex) {
            portalStockLockService.releaseForOrder(orderSn);
            if (createdOrderId != null) {
                portalCouponService.releaseByOrder(createdOrderId);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalOrderVo> pageMyOrders(Long memberId, PortalOrderQueryBo bo) {
        Page<OmsOrder> page = new Page<>(bo.getPageNum(), bo.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = Wrappers.<OmsOrder>lambdaQuery()
                .eq(OmsOrder::getMemberId, memberId)
                .eq(OmsOrder::getDeleteStatus, 0)
                .orderByDesc(OmsOrder::getCreateTime);
        if (bo.getStatus() != null) {
            wrapper.eq(OmsOrder::getStatus, bo.getStatus());
        }
        IPage<OmsOrder> orderPage = omsOrderMapper.selectPage(page, wrapper);
        List<OmsOrder> records = orderPage.getRecords();
        List<Long> orderIds = records.stream().map(OmsOrder::getId).filter(Objects::nonNull).toList();
        Map<Long, List<OmsOrderItem>> itemsByOrderId = portalOrderAssembler.loadItemsByOrderIds(orderIds);

        List<PortalOrderVo> rows = records.stream()
                .map(order -> {
                    PortalOrderVo vo = portalOrderAssembler.toOrderVoWithoutItems(order);
                    List<OmsOrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    vo.setOrderItemList(items.stream().map(portalOrderAssembler::toItemVo).collect(Collectors.toList()));
                    return vo;
                })
                .collect(Collectors.toList());

        PageResponse<PortalOrderVo> response = new PageResponse<>();
        response.setTotal(orderPage.getTotal());
        response.setRows(rows);
        response.setPageNum(orderPage.getCurrent());
        response.setPageSize(orderPage.getSize());
        response.setPageCount(orderPage.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalOrderVo getMyOrder(Long memberId, Long orderId) {
        OmsOrder order = requireMemberOrder(memberId, orderId);
        PortalOrderVo vo = portalOrderAssembler.toOrderVoWithoutItems(order);
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        vo.setOrderItemList(items.stream().map(portalOrderAssembler::toItemVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long memberId, Long orderId) {
        OmsOrder order = requireMemberOrder(memberId, orderId);
        if (!Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus())) {
            throw new BizException("仅待付款订单可取消");
        }
        order.setStatus(PortalConstants.ORDER_STATUS_CLOSED);
        order.setModifyTime(LocalDateTime.now());
        omsOrderMapper.updateById(order);
        portalStockLockService.releaseForOrder(order.getOrderSn());
        portalSeckillStockService.releaseByOrderSn(order.getOrderSn());
        portalCouponService.releaseByOrder(orderId);
        portalMemberIntegrationService.restoreForOrder(order);
        portalOrderAssembler.saveOperateHistory(
                orderId, PortalConstants.ORDER_STATUS_CLOSED, order.getMemberUsername(), "会员取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(Long memberId, Long orderId) {
        if (!mallSecurityProperties.isMockPayEnabled()) {
            throw new BizException("Mock 支付未启用");
        }
        OmsOrder order = requireMemberOrder(memberId, orderId);
        if (!Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus())) {
            throw new BizException("仅待付款订单可支付");
        }
        portalOrderPayService.confirmPaid(
                order,
                "MOCK" + System.currentTimeMillis(),
                PortalConstants.PAYMENT_STATUS_SUCCESS,
                null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long memberId, Long orderId) {
        OmsOrder order = requireMemberOrder(memberId, orderId);
        omsOrderLifecycleService.confirmReceive(order.getId(), order.getMemberUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> applyReturn(Long memberId, PortalOrderReturnApplyBo bo) {
        return portalOrderReturnService.applyReturn(memberId, bo);
    }

    private void enrichTrialLineTitles(
            PortalOrderPriceTrialVo vo, Map<Long, PortalOrderSkuSupport.SkuBase> skuBaseMap) {
        if (vo.getLines() == null) {
            return;
        }
        for (PortalOrderPriceLineVo line : vo.getLines()) {
            PortalOrderSkuSupport.SkuBase base = skuBaseMap.get(line.getSkuId());
            if (base == null) {
                continue;
            }
            String title = StringUtils.hasText(base.sku().getSkuTitle())
                    ? base.sku().getSkuTitle()
                    : base.sku().getSkuName();
            line.setSkuTitle(title);
        }
    }

    private UmsMember requireMember(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "会员不存在");
        }
        return member;
    }

    private UmsMemberReceiveAddress requireMemberAddress(Long memberId, Long addressId) {
        portalAddressService.getById(memberId, addressId);
        return addressMapper.selectById(addressId);
    }

    private OmsOrder requireMemberOrder(Long memberId, Long orderId) {
        if (orderId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus()) || !memberId.equals(order.getMemberId())) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private Map<Long, Integer> aggregateQuantityBySku(List<PortalOrderItemBo> orderItems) {
        Map<Long, Integer> quantityBySku = new LinkedHashMap<>();
        for (PortalOrderItemBo item : orderItems) {
            quantityBySku.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        return quantityBySku;
    }
}
