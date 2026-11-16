package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.OrderPortalFeignSupport;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainHelpBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainOrderBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainStartBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainHelperVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainRecordVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.service.PortalBargainService;
import cn.org.starpivot.mall.sms.constants.PromotionActivityStatus;
import cn.org.starpivot.mall.sms.entity.SmsBargainActivity;
import cn.org.starpivot.mall.sms.entity.SmsBargainHelp;
import cn.org.starpivot.mall.sms.entity.SmsBargainRecord;
import cn.org.starpivot.mall.sms.mapper.SmsBargainActivityMapper;
import cn.org.starpivot.mall.sms.mapper.SmsBargainHelpMapper;
import cn.org.starpivot.mall.sms.mapper.SmsBargainRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * C 端砍价服务实现。
 * <p>
 * 砍价阶段不占用活动库存，砍到底价后下单时才扣减库存并提交活动价订单。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortalBargainServiceImpl implements PortalBargainService {

    /** 活动类型：砍价 */
    private static final int ACTIVITY_TYPE_BARGAIN = 2;

    private static final int MAX_LIST_SIZE = 50;

    private static final int MAX_HELP_LIST_SIZE = 20;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SmsBargainActivityMapper smsBargainActivityMapper;
    private final SmsBargainRecordMapper smsBargainRecordMapper;
    private final SmsBargainHelpMapper smsBargainHelpMapper;
    private final ProductFeignSupport productFeignSupport;
    private final OrderPortalFeignSupport orderPortalFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public List<PortalBargainActivityVo> listActivities() {
        List<SmsBargainActivity> activities = smsBargainActivityMapper.selectList(
                Wrappers.<SmsBargainActivity>lambdaQuery()
                        .eq(SmsBargainActivity::getStatus, PromotionActivityStatus.ONLINE)
                        .orderByAsc(SmsBargainActivity::getSort)
                        .orderByDesc(SmsBargainActivity::getId)
                        .last("LIMIT " + MAX_LIST_SIZE));
        if (CollectionUtils.isEmpty(activities)) {
            return List.of();
        }
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(
                activities.stream().map(SmsBargainActivity::getSkuId).distinct().toList());
        LocalDateTime now = LocalDateTime.now();
        return activities.stream()
                .map(item -> toActivityVo(item, skuMap.get(item.getSkuId()), now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PortalBargainDetailVo detail(Long memberId, Long activityId) {
        SmsBargainActivity activity = requireActivity(activityId);
        SkuDto sku = activity.getSkuId() == null ? null : productFeignSupport.requireSku(activity.getSkuId());
        PortalBargainDetailVo detail = new PortalBargainDetailVo();
        detail.setActivity(toActivityVo(activity, sku, LocalDateTime.now()));
        if (memberId != null) {
            SmsBargainRecord record = findActiveRecord(activityId, memberId);
            if (record != null) {
                detail.setRecord(toRecordVo(activity, record, sku));
            }
        }
        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalBargainRecordVo recordDetail(Long recordId) {
        if (recordId == null) {
            throw new BizException("砍价记录不能为空");
        }
        SmsBargainRecord record = smsBargainRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BizException("砍价记录不存在");
        }
        SmsBargainActivity activity = smsBargainActivityMapper.selectById(record.getActivityId());
        return toRecordVo(activity, record, productFeignSupport.requireSku(record.getSkuId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalBargainRecordVo start(Long memberId, PortalBargainStartBo bo) {
        SmsBargainActivity activity = requireOnSale(bo.getActivityId());
        SmsBargainRecord existing = findActiveRecord(activity.getId(), memberId);
        if (existing != null) {
            return toRecordVo(activity, existing, productFeignSupport.requireSku(activity.getSkuId()));
        }
        SkuDto sku = productFeignSupport.requireSku(activity.getSkuId());
        BigDecimal originalPrice = activity.getOriginalPrice() != null
                ? activity.getOriginalPrice()
                : (sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice());

        LocalDateTime now = LocalDateTime.now();
        SmsBargainRecord record = new SmsBargainRecord();
        record.setActivityId(activity.getId());
        record.setMemberId(memberId);
        record.setSkuId(activity.getSkuId());
        record.setOriginalPrice(originalPrice);
        record.setCurrentPrice(originalPrice);
        record.setFloorPrice(activity.getFloorPrice());
        record.setStatus(PromotionActivityStatus.BARGAIN_ONGOING);
        record.setExpireTime(now.plusHours(activity.getValidHours() == null ? 24 : activity.getValidHours()));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        smsBargainRecordMapper.insert(record);
        return toRecordVo(activity, record, sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalBargainRecordVo help(Long memberId, PortalBargainHelpBo bo) {
        SmsBargainRecord record = smsBargainRecordMapper.selectById(bo.getRecordId());
        if (record == null) {
            throw new BizException("砍价记录不存在");
        }
        if (memberId.equals(record.getMemberId())) {
            throw new BizException("不能给自己助力，分享给好友吧");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(PromotionActivityStatus.BARGAIN_ONGOING).equals(record.getStatus())) {
            throw new BizException("该砍价已结束");
        }
        if (record.getExpireTime() != null && now.isAfter(record.getExpireTime())) {
            throw new BizException("该砍价已超时");
        }
        SmsBargainActivity activity = requireActivity(record.getActivityId());
        long helpCount = smsBargainHelpMapper.selectCount(
                Wrappers.<SmsBargainHelp>lambdaQuery().eq(SmsBargainHelp::getRecordId, record.getId()));
        int maxTimes = activity.getBargainTimes() == null ? 10 : activity.getBargainTimes();
        if (helpCount >= maxTimes) {
            throw new BizException("该砍价助力次数已用完");
        }

        BigDecimal cutAmount = randomCut(activity, record);
        BigDecimal newPrice = record.getCurrentPrice().subtract(cutAmount).max(record.getFloorPrice());
        record.setCurrentPrice(newPrice);
        record.setStatus(newPrice.compareTo(record.getFloorPrice()) <= 0
                ? PromotionActivityStatus.BARGAIN_FLOOR
                : PromotionActivityStatus.BARGAIN_ONGOING);
        record.setUpdateTime(now);
        smsBargainRecordMapper.updateById(record);

        SmsBargainHelp help = new SmsBargainHelp();
        help.setRecordId(record.getId());
        help.setHelperMemberId(memberId);
        help.setCutAmount(cutAmount);
        help.setCreateTime(now);
        try {
            smsBargainHelpMapper.insert(help);
        } catch (DuplicateKeyException exception) {
            throw new BizException("你已为该好友助力过");
        }
        return toRecordVo(activity, record, productFeignSupport.requireSku(record.getSkuId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalBargainRecordVo order(Long memberId, PortalBargainOrderBo bo) {
        SmsBargainRecord record = smsBargainRecordMapper.selectById(bo.getRecordId());
        if (record == null) {
            throw new BizException("砍价记录不存在");
        }
        if (!memberId.equals(record.getMemberId())) {
            throw new BizException("只能使用自己的砍价记录下单");
        }
        if (!Integer.valueOf(PromotionActivityStatus.BARGAIN_FLOOR).equals(record.getStatus())) {
            throw new BizException("砍到底价后才能下单");
        }
        if (record.getExpireTime() != null && LocalDateTime.now().isAfter(record.getExpireTime())) {
            throw new BizException("该砍价已超时");
        }
        SmsBargainActivity activity = requireOnSale(record.getActivityId());
        int quantity = bo.getQuantity() == null || bo.getQuantity() <= 0 ? 1 : bo.getQuantity();
        if (activity.getLimitBuy() != null && activity.getLimitBuy() > 0 && quantity > activity.getLimitBuy()) {
            throw new BizException("超过每人限购数量：" + activity.getLimitBuy());
        }
        if (smsBargainActivityMapper.decreaseStock(activity.getId(), quantity) == 0) {
            throw new BizException("活动库存不足");
        }

        PortalOrderSubmitBo submitBo = new PortalOrderSubmitBo();
        submitBo.setAddressId(bo.getAddressId());
        submitBo.setUseCart(false);
        submitBo.setOrderToken(bo.getOrderToken());
        submitBo.setPayType(bo.getPayType());
        submitBo.setNote(bo.getNote());
        submitBo.setActivityType(ACTIVITY_TYPE_BARGAIN);
        submitBo.setActivityId(activity.getId());
        submitBo.setActivityRecordId(record.getId());
        submitBo.setActivityPrice(record.getCurrentPrice());
        PortalOrderItemBo item = new PortalOrderItemBo();
        item.setSkuId(record.getSkuId());
        item.setQuantity(quantity);
        submitBo.setItems(List.of(item));
        PortalOrderSubmitVo order = orderPortalFeignSupport.submitOrder(memberId, submitBo);

        record.setStatus(PromotionActivityStatus.BARGAIN_ORDERED);
        record.setOrderId(order.getOrderId());
        record.setOrderSn(order.getOrderSn());
        record.setUpdateTime(LocalDateTime.now());
        smsBargainRecordMapper.updateById(record);
        return toRecordVo(activity, record, productFeignSupport.requireSku(record.getSkuId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalBargainRecordVo> myBargains(Long memberId) {
        List<SmsBargainRecord> records = smsBargainRecordMapper.selectList(
                Wrappers.<SmsBargainRecord>lambdaQuery()
                        .eq(SmsBargainRecord::getMemberId, memberId)
                        .orderByDesc(SmsBargainRecord::getId)
                        .last("LIMIT 20"));
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }
        List<Long> activityIds = records.stream().map(SmsBargainRecord::getActivityId).distinct().toList();
        Map<Long, SmsBargainActivity> activityMap = smsBargainActivityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(SmsBargainActivity::getId, item -> item, (a, b) -> a));
        List<Long> skuIds = records.stream().map(SmsBargainRecord::getSkuId).distinct().toList();
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(skuIds);
        return records.stream()
                .map(item -> toRecordVo(activityMap.get(item.getActivityId()), item, skuMap.get(item.getSkuId())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onOrderPaid(Long orderId) {
        if (orderId == null) {
            return;
        }
        SmsBargainRecord record = smsBargainRecordMapper.selectOne(
                Wrappers.<SmsBargainRecord>lambdaQuery().eq(SmsBargainRecord::getOrderId, orderId).last("LIMIT 1"));
        if (record == null || !Integer.valueOf(PromotionActivityStatus.BARGAIN_ORDERED).equals(record.getStatus())) {
            return;
        }
        record.setPayTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        smsBargainRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int settleExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<SmsBargainRecord> expired = smsBargainRecordMapper.selectList(
                Wrappers.<SmsBargainRecord>lambdaQuery()
                        .in(SmsBargainRecord::getStatus,
                                PromotionActivityStatus.BARGAIN_ONGOING,
                                PromotionActivityStatus.BARGAIN_FLOOR)
                        .lt(SmsBargainRecord::getExpireTime, now)
                        .last("LIMIT 200"));
        for (SmsBargainRecord record : expired) {
            record.setStatus(PromotionActivityStatus.BARGAIN_EXPIRED);
            record.setUpdateTime(now);
            smsBargainRecordMapper.updateById(record);
        }
        return expired.size();
    }

    private SmsBargainRecord findActiveRecord(Long activityId, Long memberId) {
        return smsBargainRecordMapper.selectOne(
                Wrappers.<SmsBargainRecord>lambdaQuery()
                        .eq(SmsBargainRecord::getActivityId, activityId)
                        .eq(SmsBargainRecord::getMemberId, memberId)
                        .in(SmsBargainRecord::getStatus,
                                PromotionActivityStatus.BARGAIN_ONGOING,
                                PromotionActivityStatus.BARGAIN_FLOOR)
                        .orderByDesc(SmsBargainRecord::getId)
                        .last("LIMIT 1"));
    }

    /**
     * 在 [minAmount, maxAmount] 区间内随机砍价，且不超过剩余可砍金额。
     */
    private BigDecimal randomCut(SmsBargainActivity activity, SmsBargainRecord record) {
        BigDecimal remaining = record.getCurrentPrice().subtract(record.getFloorPrice());
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("该砍价已到底价");
        }
        BigDecimal min = activity.getMinAmount() == null ? BigDecimal.ONE : activity.getMinAmount();
        BigDecimal max = activity.getMaxAmount() == null ? min : activity.getMaxAmount();
        if (max.compareTo(min) < 0) {
            max = min;
        }
        long minCents = min.multiply(ONE_HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
        long maxCents = max.multiply(ONE_HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
        long cutCents = minCents >= maxCents
                ? Math.max(minCents, 1)
                : ThreadLocalRandom.current().nextLong(minCents, maxCents + 1);
        BigDecimal cut = BigDecimal.valueOf(cutCents, 2);
        return cut.min(remaining);
    }

    private SmsBargainActivity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new BizException("砍价活动不能为空");
        }
        SmsBargainActivity activity = smsBargainActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException("砍价活动不存在");
        }
        return activity;
    }

    private SmsBargainActivity requireOnSale(Long activityId) {
        SmsBargainActivity activity = requireActivity(activityId);
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(PromotionActivityStatus.ONLINE).equals(activity.getStatus())) {
            throw new BizException("砍价活动已下线");
        }
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BizException("砍价活动未开始");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BizException("砍价活动已结束");
        }
        return activity;
    }

    private PortalBargainActivityVo toActivityVo(SmsBargainActivity activity, SkuDto sku, LocalDateTime now) {
        PortalBargainActivityVo vo = new PortalBargainActivityVo();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setSkuId(activity.getSkuId());
        vo.setOriginalPrice(activity.getOriginalPrice());
        vo.setFloorPrice(activity.getFloorPrice());
        vo.setBargainTimes(activity.getBargainTimes());
        vo.setMinAmount(activity.getMinAmount());
        vo.setMaxAmount(activity.getMaxAmount());
        vo.setRemainStock(activity.getStockCount());
        vo.setLimitBuy(activity.getLimitBuy());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setValidHours(activity.getValidHours());
        vo.setState(resolveState(activity.getStartTime(), activity.getEndTime(), now));
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
            if (vo.getOriginalPrice() == null) {
                vo.setOriginalPrice(sku.getPrice());
            }
        }
        return vo;
    }

    private PortalBargainRecordVo toRecordVo(SmsBargainActivity activity, SmsBargainRecord record, SkuDto sku) {
        PortalBargainRecordVo vo = new PortalBargainRecordVo();
        vo.setId(record.getId());
        vo.setActivityId(record.getActivityId());
        vo.setSkuId(record.getSkuId());
        vo.setOriginalPrice(record.getOriginalPrice());
        vo.setCurrentPrice(record.getCurrentPrice());
        vo.setFloorPrice(record.getFloorPrice());
        vo.setStatus(record.getStatus());
        vo.setStatusText(bargainStatusText(record.getStatus()));
        vo.setExpireTime(record.getExpireTime());
        vo.setOrderId(record.getOrderId());
        vo.setOrderSn(record.getOrderSn());
        vo.setCreateTime(record.getCreateTime());
        if (activity != null) {
            vo.setTitle(activity.getTitle());
            vo.setBargainTimes(activity.getBargainTimes());
        }
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
        }
        List<SmsBargainHelp> helps = smsBargainHelpMapper.selectList(
                Wrappers.<SmsBargainHelp>lambdaQuery()
                        .eq(SmsBargainHelp::getRecordId, record.getId())
                        .orderByDesc(SmsBargainHelp::getId)
                        .last("LIMIT " + MAX_HELP_LIST_SIZE));
        long helpCount = smsBargainHelpMapper.selectCount(
                Wrappers.<SmsBargainHelp>lambdaQuery().eq(SmsBargainHelp::getRecordId, record.getId()));
        vo.setHelpCount((int) helpCount);
        int maxTimes = activity == null || activity.getBargainTimes() == null ? 0 : activity.getBargainTimes();
        vo.setRemainHelps(Math.max(maxTimes - (int) helpCount, 0));
        vo.setHelpers(helps.stream().map(item -> {
            PortalBargainHelperVo helper = new PortalBargainHelperVo();
            helper.setHelperMemberId(item.getHelperMemberId());
            helper.setCutAmount(item.getCutAmount());
            return helper;
        }).toList());
        return vo;
    }

    private String resolveState(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (startTime != null && now.isBefore(startTime)) {
            return "upcoming";
        }
        if (endTime != null && now.isAfter(endTime)) {
            return "ended";
        }
        return "ongoing";
    }

    private String bargainStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PromotionActivityStatus.BARGAIN_ONGOING -> "砍价中";
            case PromotionActivityStatus.BARGAIN_FLOOR -> "已到底价";
            case PromotionActivityStatus.BARGAIN_ORDERED -> "已下单";
            case PromotionActivityStatus.BARGAIN_EXPIRED -> "已失效";
            default -> "";
        };
    }
}