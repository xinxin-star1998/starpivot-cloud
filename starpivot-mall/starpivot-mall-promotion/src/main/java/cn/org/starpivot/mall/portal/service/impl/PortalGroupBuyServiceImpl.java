package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.OrderPortalFeignSupport;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyJoinBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyStartBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyGroupVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyRecordVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.service.PortalGroupBuyService;
import cn.org.starpivot.mall.sms.constants.PromotionActivityStatus;
import cn.org.starpivot.mall.sms.entity.SmsGroupBuyActivity;
import cn.org.starpivot.mall.sms.entity.SmsGroupBuyRecord;
import cn.org.starpivot.mall.sms.mapper.SmsGroupBuyActivityMapper;
import cn.org.starpivot.mall.sms.mapper.SmsGroupBuyRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * C 端拼团服务实现。
 * <p>
 * 开团/参团均先扣减活动库存，再以拼团价提交订单；支付成功事件由 MQ 回调推进团状态。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortalGroupBuyServiceImpl implements PortalGroupBuyService {

    /** 待支付超时分钟数 */
    private static final int PAY_TIMEOUT_MINUTES = 30;

    /** 活动类型：拼团 */
    private static final int ACTIVITY_TYPE_GROUP_BUY = 1;

    /** 列表最多返回条数 */
    private static final int MAX_LIST_SIZE = 50;

    private static final DateTimeFormatter GROUP_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SmsGroupBuyActivityMapper smsGroupBuyActivityMapper;
    private final SmsGroupBuyRecordMapper smsGroupBuyRecordMapper;
    private final ProductFeignSupport productFeignSupport;
    private final OrderPortalFeignSupport orderPortalFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public List<PortalGroupBuyActivityVo> listActivities() {
        List<SmsGroupBuyActivity> activities = smsGroupBuyActivityMapper.selectList(
                Wrappers.<SmsGroupBuyActivity>lambdaQuery()
                        .eq(SmsGroupBuyActivity::getStatus, PromotionActivityStatus.ONLINE)
                        .orderByAsc(SmsGroupBuyActivity::getSort)
                        .orderByDesc(SmsGroupBuyActivity::getId)
                        .last("LIMIT " + MAX_LIST_SIZE));
        if (CollectionUtils.isEmpty(activities)) {
            return List.of();
        }
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(
                activities.stream().map(SmsGroupBuyActivity::getSkuId).distinct().toList());
        LocalDateTime now = LocalDateTime.now();
        return activities.stream()
                .map(item -> toActivityVo(item, skuMap.get(item.getSkuId()), now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PortalGroupBuyDetailVo detail(Long activityId) {
        SmsGroupBuyActivity activity = requireActivity(activityId);
        LocalDateTime now = LocalDateTime.now();
        SkuDto sku = activity.getSkuId() == null ? null : productFeignSupport.requireSku(activity.getSkuId());

        PortalGroupBuyDetailVo detail = new PortalGroupBuyDetailVo();
        detail.setActivity(toActivityVo(activity, sku, now));
        detail.setGroups(loadJoinableGroups(activityId, now));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalGroupBuyRecordVo start(Long memberId, PortalGroupBuyStartBo bo) {
        SmsGroupBuyActivity activity = requireOnSale(bo.getActivityId());
        int quantity = normalizeQuantity(bo.getQuantity(), activity.getLimitBuy());
        if (smsGroupBuyActivityMapper.decreaseStock(activity.getId(), quantity) == 0) {
            throw new BizException("活动库存不足");
        }

        LocalDateTime now = LocalDateTime.now();
        String groupNo = generateGroupNo();
        SmsGroupBuyRecord record = new SmsGroupBuyRecord();
        record.setActivityId(activity.getId());
        record.setMemberId(memberId);
        record.setLeaderMemberId(memberId);
        record.setSkuId(activity.getSkuId());
        record.setGroupNo(groupNo);
        record.setGroupSize(activity.getGroupSize());
        record.setGroupPrice(activity.getGroupPrice());
        record.setQuantity(quantity);
        record.setStatus(PromotionActivityStatus.GROUP_PENDING_PAY);
        record.setExpireTime(now.plusHours(activity.getValidHours() == null ? 24 : activity.getValidHours()));
        record.setPayExpireTime(now.plusMinutes(PAY_TIMEOUT_MINUTES));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        smsGroupBuyRecordMapper.insert(record);

        PortalOrderSubmitVo order = submitActivityOrder(memberId, activity, record, bo.getAddressId(),
                bo.getOrderToken(), bo.getPayType(), bo.getNote());
        record.setOrderId(order.getOrderId());
        record.setOrderSn(order.getOrderSn());
        record.setUpdateTime(LocalDateTime.now());
        smsGroupBuyRecordMapper.updateById(record);

        return toRecordVo(record, activity, productFeignSupport.requireSku(activity.getSkuId()), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalGroupBuyRecordVo join(Long memberId, PortalGroupBuyJoinBo bo) {
        List<SmsGroupBuyRecord> records = loadGroupRecords(bo.getGroupNo());
        if (CollectionUtils.isEmpty(records)) {
            throw new BizException("团不存在");
        }
        SmsGroupBuyRecord leader = records.stream()
                .filter(item -> item.getLeaderMemberId() != null
                        && item.getLeaderMemberId().equals(item.getMemberId()))
                .findFirst()
                .orElse(null);
        if (leader == null) {
            throw new BizException("团不存在");
        }
        if (!Integer.valueOf(PromotionActivityStatus.GROUP_WAIT_GROUP).equals(leader.getStatus())) {
            throw new BizException("该团尚未支付成功，暂时无法参与");
        }
        if (records.stream().anyMatch(item -> memberId.equals(item.getMemberId()))) {
            throw new BizException("你已参与该团，无需重复参团");
        }
        long joined = records.stream()
                .filter(item -> item.getStatus() != null
                        && (item.getStatus() == PromotionActivityStatus.GROUP_PENDING_PAY
                        || item.getStatus() == PromotionActivityStatus.GROUP_WAIT_GROUP))
                .count();
        if (joined >= leader.getGroupSize()) {
            throw new BizException("该团人数已满");
        }
        if (leader.getExpireTime() != null && LocalDateTime.now().isAfter(leader.getExpireTime())) {
            throw new BizException("该团已结束");
        }

        SmsGroupBuyActivity activity = requireOnSale(leader.getActivityId());
        int quantity = normalizeQuantity(bo.getQuantity(), activity.getLimitBuy());
        if (smsGroupBuyActivityMapper.decreaseStock(activity.getId(), quantity) == 0) {
            throw new BizException("活动库存不足");
        }

        LocalDateTime now = LocalDateTime.now();
        SmsGroupBuyRecord record = new SmsGroupBuyRecord();
        record.setActivityId(activity.getId());
        record.setMemberId(memberId);
        record.setLeaderMemberId(leader.getLeaderMemberId());
        record.setSkuId(activity.getSkuId());
        record.setGroupNo(leader.getGroupNo());
        record.setGroupSize(leader.getGroupSize());
        record.setGroupPrice(leader.getGroupPrice());
        record.setQuantity(quantity);
        record.setStatus(PromotionActivityStatus.GROUP_PENDING_PAY);
        record.setExpireTime(leader.getExpireTime());
        record.setPayExpireTime(now.plusMinutes(PAY_TIMEOUT_MINUTES));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        smsGroupBuyRecordMapper.insert(record);

        PortalOrderSubmitVo order = submitActivityOrder(memberId, activity, record, bo.getAddressId(),
                bo.getOrderToken(), bo.getPayType(), bo.getNote());
        record.setOrderId(order.getOrderId());
        record.setOrderSn(order.getOrderSn());
        record.setUpdateTime(LocalDateTime.now());
        smsGroupBuyRecordMapper.updateById(record);

        return toRecordVo(record, activity, productFeignSupport.requireSku(activity.getSkuId()),
                (int) joined + 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalGroupBuyRecordVo> myGroups(Long memberId) {
        List<SmsGroupBuyRecord> records = smsGroupBuyRecordMapper.selectList(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery()
                        .eq(SmsGroupBuyRecord::getMemberId, memberId)
                        .orderByDesc(SmsGroupBuyRecord::getId)
                        .last("LIMIT 20"));
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }
        List<Long> activityIds = records.stream().map(SmsGroupBuyRecord::getActivityId).distinct().toList();
        Map<Long, SmsGroupBuyActivity> activityMap = smsGroupBuyActivityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(SmsGroupBuyActivity::getId, item -> item, (a, b) -> a));
        List<Long> groupNos = records.stream().map(SmsGroupBuyRecord::getGroupNo).distinct().toList();
        Map<String, Long> joinedMap = smsGroupBuyRecordMapper.selectList(Wrappers.<SmsGroupBuyRecord>lambdaQuery()
                        .in(SmsGroupBuyRecord::getGroupNo, groupNos)
                        .in(SmsGroupBuyRecord::getStatus,
                                PromotionActivityStatus.GROUP_PENDING_PAY,
                                PromotionActivityStatus.GROUP_WAIT_GROUP,
                                PromotionActivityStatus.GROUP_COMPLETED))
                .stream()
                .collect(Collectors.groupingBy(SmsGroupBuyRecord::getGroupNo, Collectors.counting()));

        List<Long> skuIds = records.stream().map(SmsGroupBuyRecord::getSkuId).distinct().toList();
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(skuIds);
        return records.stream()
                .map(item -> toRecordVo(item, activityMap.get(item.getActivityId()),
                        skuMap.get(item.getSkuId()), joinedMap.getOrDefault(item.getGroupNo(), 1L).intValue()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onOrderPaid(Long orderId) {
        if (orderId == null) {
            return;
        }
        SmsGroupBuyRecord record = smsGroupBuyRecordMapper.selectOne(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery().eq(SmsGroupBuyRecord::getOrderId, orderId).last("LIMIT 1"));
        if (record == null || !Integer.valueOf(PromotionActivityStatus.GROUP_PENDING_PAY).equals(record.getStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        record.setStatus(PromotionActivityStatus.GROUP_WAIT_GROUP);
        record.setPayTime(now);
        record.setUpdateTime(now);
        smsGroupBuyRecordMapper.updateById(record);

        List<SmsGroupBuyRecord> groupRecords = loadGroupRecords(record.getGroupNo());
        long paidCount = groupRecords.stream()
                .filter(item -> item.getStatus() != null
                        && (item.getStatus() == PromotionActivityStatus.GROUP_WAIT_GROUP
                        || item.getStatus() == PromotionActivityStatus.GROUP_COMPLETED))
                .count();
        if (paidCount >= record.getGroupSize()) {
            groupRecords.stream()
                    .filter(item -> Integer.valueOf(PromotionActivityStatus.GROUP_WAIT_GROUP).equals(item.getStatus()))
                    .forEach(item -> {
                        item.setStatus(PromotionActivityStatus.GROUP_COMPLETED);
                        item.setCompleteTime(now);
                        item.setUpdateTime(now);
                        smsGroupBuyRecordMapper.updateById(item);
                    });
            log.info("拼团团 {} 已成团，人数={}", record.getGroupNo(), paidCount);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int settleExpired() {
        LocalDateTime now = LocalDateTime.now();
        int handled = 0;

        List<SmsGroupBuyRecord> unpaid = smsGroupBuyRecordMapper.selectList(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery()
                        .eq(SmsGroupBuyRecord::getStatus, PromotionActivityStatus.GROUP_PENDING_PAY)
                        .lt(SmsGroupBuyRecord::getPayExpireTime, now)
                        .last("LIMIT 200"));
        for (SmsGroupBuyRecord record : unpaid) {
            record.setStatus(PromotionActivityStatus.GROUP_CANCELED);
            record.setUpdateTime(now);
            smsGroupBuyRecordMapper.updateById(record);
            smsGroupBuyActivityMapper.increaseStock(record.getActivityId(), safeQuantity(record.getQuantity()));
            handled++;
        }

        List<SmsGroupBuyRecord> failed = smsGroupBuyRecordMapper.selectList(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery()
                        .eq(SmsGroupBuyRecord::getStatus, PromotionActivityStatus.GROUP_WAIT_GROUP)
                        .lt(SmsGroupBuyRecord::getExpireTime, now)
                        .last("LIMIT 200"));
        for (SmsGroupBuyRecord record : failed) {
            record.setStatus(PromotionActivityStatus.GROUP_FAILED);
            record.setUpdateTime(now);
            smsGroupBuyRecordMapper.updateById(record);
            smsGroupBuyActivityMapper.increaseStock(record.getActivityId(), safeQuantity(record.getQuantity()));
            log.warn("拼团团 {} 超时未成团，订单 {} 需退款处理", record.getGroupNo(), record.getOrderSn());
            handled++;
        }
        return handled;
    }

    private PortalOrderSubmitVo submitActivityOrder(Long memberId, SmsGroupBuyActivity activity,
                                                    SmsGroupBuyRecord record, Long addressId,
                                                    String orderToken, Integer payType, String note) {
        PortalOrderSubmitBo submitBo = new PortalOrderSubmitBo();
        submitBo.setAddressId(addressId);
        submitBo.setUseCart(false);
        submitBo.setOrderToken(orderToken);
        submitBo.setPayType(payType);
        submitBo.setNote(note);
        submitBo.setActivityType(ACTIVITY_TYPE_GROUP_BUY);
        submitBo.setActivityId(activity.getId());
        submitBo.setActivityRecordId(record.getId());
        submitBo.setActivityPrice(activity.getGroupPrice());

        PortalOrderItemBo item = new PortalOrderItemBo();
        item.setSkuId(record.getSkuId());
        item.setQuantity(record.getQuantity());
        submitBo.setItems(List.of(item));
        return orderPortalFeignSupport.submitOrder(memberId, submitBo);
    }

    private List<PortalGroupBuyGroupVo> loadJoinableGroups(Long activityId, LocalDateTime now) {
        List<SmsGroupBuyRecord> records = smsGroupBuyRecordMapper.selectList(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery()
                        .eq(SmsGroupBuyRecord::getActivityId, activityId)
                        .in(SmsGroupBuyRecord::getStatus,
                                PromotionActivityStatus.GROUP_PENDING_PAY,
                                PromotionActivityStatus.GROUP_WAIT_GROUP)
                        .gt(SmsGroupBuyRecord::getExpireTime, now));
        Map<String, List<SmsGroupBuyRecord>> groupMap = records.stream()
                .collect(Collectors.groupingBy(SmsGroupBuyRecord::getGroupNo));
        return groupMap.values().stream()
                .map(group -> {
                    SmsGroupBuyRecord leader = group.stream()
                            .filter(item -> item.getLeaderMemberId() != null
                                    && item.getLeaderMemberId().equals(item.getMemberId()))
                            .findFirst()
                            .orElse(null);
                    if (leader == null
                            || !Integer.valueOf(PromotionActivityStatus.GROUP_WAIT_GROUP).equals(leader.getStatus())) {
                        return null;
                    }
                    int joined = group.size();
                    if (joined >= leader.getGroupSize()) {
                        return null;
                    }
                    PortalGroupBuyGroupVo vo = new PortalGroupBuyGroupVo();
                    vo.setGroupNo(leader.getGroupNo());
                    vo.setLeaderMemberId(leader.getLeaderMemberId());
                    vo.setJoinedCount(joined);
                    vo.setGroupSize(leader.getGroupSize());
                    vo.setRemainCount(Math.max(leader.getGroupSize() - joined, 0));
                    vo.setExpireTime(leader.getExpireTime());
                    vo.setRemainSeconds(Math.max(Duration.between(now, leader.getExpireTime()).getSeconds(), 0));
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<SmsGroupBuyRecord> loadGroupRecords(String groupNo) {
        return smsGroupBuyRecordMapper.selectList(
                Wrappers.<SmsGroupBuyRecord>lambdaQuery().eq(SmsGroupBuyRecord::getGroupNo, groupNo));
    }

    private SmsGroupBuyActivity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new BizException("拼团活动不能为空");
        }
        SmsGroupBuyActivity activity = smsGroupBuyActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException("拼团活动不存在");
        }
        return activity;
    }

    private SmsGroupBuyActivity requireOnSale(Long activityId) {
        SmsGroupBuyActivity activity = requireActivity(activityId);
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(PromotionActivityStatus.ONLINE).equals(activity.getStatus())) {
            throw new BizException("拼团活动已下线");
        }
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BizException("拼团活动未开始");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BizException("拼团活动已结束");
        }
        return activity;
    }

    private int normalizeQuantity(Integer quantity, Integer limitBuy) {
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;
        if (limitBuy != null && limitBuy > 0 && qty > limitBuy) {
            throw new BizException("超过每人限购数量：" + limitBuy);
        }
        return qty;
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null || quantity <= 0 ? 1 : quantity;
    }

    private String generateGroupNo() {
        return "GB" + LocalDateTime.now().format(GROUP_NO_FORMATTER)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private PortalGroupBuyActivityVo toActivityVo(SmsGroupBuyActivity activity, SkuDto sku, LocalDateTime now) {
        PortalGroupBuyActivityVo vo = new PortalGroupBuyActivityVo();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setSkuId(activity.getSkuId());
        vo.setGroupPrice(activity.getGroupPrice());
        vo.setGroupSize(activity.getGroupSize());
        vo.setRemainStock(activity.getStockCount());
        vo.setLimitBuy(activity.getLimitBuy());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setValidHours(activity.getValidHours());
        vo.setState(resolveState(activity.getStartTime(), activity.getEndTime(), now));
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
            vo.setOriginalPrice(sku.getPrice());
        }
        return vo;
    }

    private PortalGroupBuyRecordVo toRecordVo(SmsGroupBuyRecord record, SmsGroupBuyActivity activity,
                                              SkuDto sku, int joinedCount) {
        PortalGroupBuyRecordVo vo = new PortalGroupBuyRecordVo();
        vo.setId(record.getId());
        vo.setActivityId(record.getActivityId());
        vo.setGroupNo(record.getGroupNo());
        vo.setGroupPrice(record.getGroupPrice());
        vo.setQuantity(record.getQuantity());
        vo.setStatus(record.getStatus());
        vo.setStatusText(groupStatusText(record.getStatus()));
        vo.setLeader(record.isLeader());
        vo.setJoinedCount(joinedCount);
        vo.setGroupSize(record.getGroupSize());
        vo.setExpireTime(record.getExpireTime());
        vo.setOrderId(record.getOrderId());
        vo.setOrderSn(record.getOrderSn());
        vo.setCreateTime(record.getCreateTime());
        if (activity != null) {
            vo.setTitle(activity.getTitle());
        }
        if (sku != null) {
            vo.setSkuId(sku.getSkuId());
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
        }
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

    private String groupStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PromotionActivityStatus.GROUP_PENDING_PAY -> "待支付";
            case PromotionActivityStatus.GROUP_WAIT_GROUP -> "待成团";
            case PromotionActivityStatus.GROUP_COMPLETED -> "已成团";
            case PromotionActivityStatus.GROUP_FAILED -> "成团失败";
            case PromotionActivityStatus.GROUP_CANCELED -> "已取消";
            default -> "";
        };
    }
}
