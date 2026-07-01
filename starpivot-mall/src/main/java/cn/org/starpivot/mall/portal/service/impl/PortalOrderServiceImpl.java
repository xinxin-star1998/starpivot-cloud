package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.config.MallSecurityProperties;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderItemVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.service.impl.OmsOrderLifecycleService;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSkuSaleAttrValue;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuSaleAttrValueMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.*;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceLineVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceTrialVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitTokenVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;
import cn.org.starpivot.mall.portal.service.*;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现类。
 * <p>
 * 实现 {@link PortalOrderService}，处理订单相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PortalOrderService
 */

@Service
@RequiredArgsConstructor
public class PortalOrderServiceImpl implements PortalOrderService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberReceiveAddressMapper addressMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsBrandMapper pmsBrandMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PortalCartService portalCartService;
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
        List<PortalOrderItemBo> orderItems = resolveTrialItems(memberId, bo);
        PortalOrderPriceTrialBo trialBo = new PortalOrderPriceTrialBo();
        trialBo.setItems(orderItems);
        trialBo.setCouponHistoryId(bo.getCouponHistoryId());
        trialBo.setUseIntegration(bo.getUseIntegration());
        PortalOrderPriceTrialVo vo = portalOrderPricingService.trial(memberId, trialBo);
        enrichTrialLineTitles(vo, loadSkuBaseMap(orderItems));
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
        List<PortalOrderItemBo> orderItems = resolveOrderItems(memberId, bo);
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BizException("下单商品不能为空");
        }

        PortalOrderPricingResult pricing = portalOrderPricingService.calculate(
                memberId,
                orderItems,
                bo.getCouponHistoryId(),
                bo.getUseIntegration(),
                true);

        String orderSn = generateOrderSn();
        LocalDateTime now = LocalDateTime.now();
        List<OmsOrderItem> itemEntities = new ArrayList<>();
        Map<Long, Integer> qtyBySku = aggregateQuantityBySku(orderItems);
        Map<Long, SkuBase> skuBaseMap = loadSkuBaseMap(orderItems);
        List<PortalOrderPricingResult.PricedLine> pricedLines = pricing.getLines();

        for (int i = 0; i < orderItems.size(); i++) {
            PortalOrderItemBo itemBo = orderItems.get(i);
            SkuBase base = skuBaseMap.get(itemBo.getSkuId());
            if (base == null) {
                throw new BizException("SKU不存在：" + itemBo.getSkuId());
            }
            PortalOrderPricingResult.PricedLine priced = i < pricedLines.size() ? pricedLines.get(i) : null;
            BigDecimal unitPrice = priced != null ? priced.getUnitPrice() : base.sku().getPrice();
            BigDecimal lineAmount = priced != null
                    ? priced.getLineAmount()
                    : unitPrice.multiply(BigDecimal.valueOf(itemBo.getQuantity()));
            BigDecimal linePromotion = priced != null ? priced.getPromotionAmount() : BigDecimal.ZERO;
            SkuSnapshot snapshot = toSnapshot(base, itemBo.getQuantity(), unitPrice, lineAmount);
            itemEntities.add(buildOrderItem(orderSn, itemBo.getQuantity(), snapshot, linePromotion));
        }

        portalStockLockService.lockForOrder(orderSn, qtyBySku);
        Long createdOrderId = null;
        try {
            OmsOrder order = buildOrder(member, address, bo, orderSn, now, pricing);
            omsOrderMapper.insert(order);
            createdOrderId = order.getId();
            for (OmsOrderItem item : itemEntities) {
                item.setOrderId(order.getId());
                omsOrderItemMapper.insert(item);
            }
            portalCouponService.lockToOrder(bo.getCouponHistoryId(), memberId, order.getId(), orderSn);
            portalMemberIntegrationService.deductForOrder(order);
            saveOperateHistory(order.getId(), PortalConstants.ORDER_STATUS_UNPAID, member.getUsername(), "提交订单");

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
        Map<Long, List<OmsOrderItem>> itemsByOrderId = loadItemsByOrderIds(orderIds);

        List<PortalOrderVo> rows = records.stream()
                .map(order -> {
                    PortalOrderVo vo = toOrderVoWithoutItems(order);
                    List<OmsOrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    vo.setOrderItemList(items.stream().map(this::toItemVo).collect(Collectors.toList()));
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
        PortalOrderVo vo = toOrderVoWithoutItems(order);
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        vo.setOrderItemList(items.stream().map(this::toItemVo).collect(Collectors.toList()));
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
        saveOperateHistory(orderId, PortalConstants.ORDER_STATUS_CLOSED, order.getMemberUsername(), "会员取消订单");
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

    private List<PortalOrderItemBo> resolveTrialItems(Long memberId, PortalOrderPriceTrialBo bo) {
        if (Boolean.TRUE.equals(bo.getUseCart())) {
            PortalCartVo cart = portalCartService.listCart(memberId);
            if (cart.getItems() == null) {
                return List.of();
            }
            return cart.getItems().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getChecked()))
                    .filter(item -> Boolean.TRUE.equals(item.getValid()))
                    .filter(item -> bo.getCartSkuIds() == null
                            || bo.getCartSkuIds().isEmpty()
                            || bo.getCartSkuIds().contains(item.getSkuId()))
                    .map(item -> {
                        PortalOrderItemBo orderItem = new PortalOrderItemBo();
                        orderItem.setSkuId(item.getSkuId());
                        orderItem.setQuantity(item.getQuantity());
                        return orderItem;
                    })
                    .collect(Collectors.toList());
        }
        return bo.getItems() == null ? List.of() : bo.getItems();
    }

    private void enrichTrialLineTitles(PortalOrderPriceTrialVo vo, Map<Long, SkuBase> skuBaseMap) {
        if (vo.getLines() == null) {
            return;
        }
        for (PortalOrderPriceLineVo line : vo.getLines()) {
            SkuBase base = skuBaseMap.get(line.getSkuId());
            if (base == null) {
                continue;
            }
            String title = StringUtils.hasText(base.sku().getSkuTitle())
                    ? base.sku().getSkuTitle()
                    : base.sku().getSkuName();
            line.setSkuTitle(title);
        }
    }

    private List<PortalOrderItemBo> resolveOrderItems(Long memberId, PortalOrderSubmitBo bo) {
        if (Boolean.TRUE.equals(bo.getUseCart())) {
            PortalCartVo cart = portalCartService.listCart(memberId);
            if (cart.getItems() == null) {
                return List.of();
            }
            return cart.getItems().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getChecked()))
                    .filter(item -> Boolean.TRUE.equals(item.getValid()))
                    .filter(item -> bo.getCartSkuIds() == null
                            || bo.getCartSkuIds().isEmpty()
                            || bo.getCartSkuIds().contains(item.getSkuId()))
                    .map(item -> {
                        PortalOrderItemBo orderItem = new PortalOrderItemBo();
                        orderItem.setSkuId(item.getSkuId());
                        orderItem.setQuantity(item.getQuantity());
                        return orderItem;
                    })
                    .collect(Collectors.toList());
        }
        return bo.getItems() == null ? List.of() : bo.getItems();
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

    private Map<Long, SkuBase> loadSkuBaseMap(List<PortalOrderItemBo> orderItems) {
        List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();

        List<PmsSkuInfo> skus = skuIds.isEmpty() ? List.of() : pmsSkuInfoMapper.selectBatchIds(skuIds);
        Map<Long, PmsSkuInfo> skuMap = skus.stream()
                .filter(sku -> sku.getSkuId() != null)
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, sku -> sku, (a, b) -> a));

        List<Long> spuIds = skus.stream()
                .map(PmsSkuInfo::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, PmsSpuInfo> spuMap = spuIds.isEmpty()
                ? Map.of()
                : pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                        .filter(spu -> spu.getId() != null)
                        .collect(Collectors.toMap(PmsSpuInfo::getId, spu -> spu, (a, b) -> a));

        List<Long> brandIds = spuMap.values().stream()
                .map(PmsSpuInfo::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> brandNameMap = brandIds.isEmpty()
                ? Map.of()
                : pmsBrandMapper.selectBatchIds(brandIds).stream()
                        .filter(brand -> brand.getBrandId() != null)
                        .collect(Collectors.toMap(PmsBrand::getBrandId, PmsBrand::getName, (a, b) -> a));

        Map<Long, String> attrMap = loadSkuAttrMap(skuIds);

        Map<Long, SkuBase> result = new LinkedHashMap<>();
        for (Long skuId : skuIds) {
            PmsSkuInfo sku = skuMap.get(skuId);
            if (sku == null) {
                throw new BizException("SKU不存在：" + skuId);
            }
            PmsSpuInfo spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
            if (spu == null || !Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
                throw new BizException("商品已下架：" + skuId);
            }
            if (sku.getPrice() == null) {
                throw new BizException("商品价格异常：" + skuId);
            }
            String brandName = spu.getBrandId() == null ? null : brandNameMap.get(spu.getBrandId());
            result.put(skuId, new SkuBase(sku, spu, brandName, attrMap.getOrDefault(skuId, "")));
        }
        return result;
    }

    private Map<Long, String> loadSkuAttrMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        List<PmsSkuSaleAttrValue> attrs = pmsSkuSaleAttrValueMapper.selectList(
                Wrappers.<PmsSkuSaleAttrValue>lambdaQuery()
                        .in(PmsSkuSaleAttrValue::getSkuId, skuIds)
                        .orderByAsc(PmsSkuSaleAttrValue::getAttrSort));
        Map<Long, StringBuilder> builderMap = new LinkedHashMap<>();
        for (PmsSkuSaleAttrValue attr : attrs) {
            if (attr.getSkuId() == null) {
                continue;
            }
            StringBuilder sb = builderMap.computeIfAbsent(attr.getSkuId(), k -> new StringBuilder());
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(attr.getAttrName()).append(':').append(attr.getAttrValue());
        }
        Map<Long, String> result = new LinkedHashMap<>();
        builderMap.forEach((skuId, sb) -> result.put(skuId, sb.toString()));
        return result;
    }

    private SkuSnapshot toSnapshot(SkuBase base, int quantity, BigDecimal unitPrice, BigDecimal lineAmount) {
        return new SkuSnapshot(base.sku(), base.spu(), base.brandName(), base.attrs(), lineAmount, unitPrice);
    }

    private OmsOrder buildOrder(
            UmsMember member,
            UmsMemberReceiveAddress address,
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

    private OmsOrderItem buildOrderItem(String orderSn, int quantity, SkuSnapshot snapshot, BigDecimal linePromotion) {
        OmsOrderItem item = new OmsOrderItem();
        item.setOrderSn(orderSn);
        item.setSpuId(snapshot.spu().getId());
        item.setSpuName(snapshot.spu().getSpuName());
        item.setCategoryId(snapshot.spu().getCatalogId());
        item.setSpuBrand(snapshot.brandName());
        item.setSkuId(snapshot.sku().getSkuId());
        item.setSkuName(StringUtils.hasText(snapshot.sku().getSkuTitle())
                ? snapshot.sku().getSkuTitle()
                : snapshot.sku().getSkuName());
        item.setSkuPrice(snapshot.unitPrice() != null ? snapshot.unitPrice() : snapshot.sku().getPrice());
        item.setSkuQuantity(quantity);
        item.setSkuAttrsVals(snapshot.attrs());
        item.setPromotionAmount(linePromotion == null ? BigDecimal.ZERO : linePromotion);
        item.setCouponAmount(BigDecimal.ZERO);
        item.setIntegrationAmount(BigDecimal.ZERO);
        item.setRealAmount(snapshot.lineAmount());
        if (StringUtils.hasText(snapshot.sku().getSkuDefaultImg())) {
            item.setSkuPic(StorageObjectPathUtils.normalizeToObjectName(snapshot.sku().getSkuDefaultImg()));
            item.setSpuPic(item.getSkuPic());
        }
        return item;
    }

    private void saveOperateHistory(Long orderId, Integer status, String operator, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }

    private String generateOrderSn() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return PortalConstants.ORDER_SN_PREFIX + timePart + randomPart;
    }

    private PortalOrderVo toOrderVoWithoutItems(OmsOrder order) {
        PortalOrderVo vo = new PortalOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private OmsOrderItemVo toItemVo(OmsOrderItem item) {
        OmsOrderItemVo vo = new OmsOrderItemVo();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private Map<Long, List<OmsOrderItem>> loadItemsByOrderIds(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().in(OmsOrderItem::getOrderId, orderIds));
        return items.stream().collect(Collectors.groupingBy(OmsOrderItem::getOrderId));
    }

    private record SkuBase(PmsSkuInfo sku, PmsSpuInfo spu, String brandName, String attrs) {}

    private record SkuSnapshot(
            PmsSkuInfo sku,
            PmsSpuInfo spu,
            String brandName,
            String attrs,
            BigDecimal lineAmount,
            BigDecimal unitPrice) {
    }
}
