package cn.org.starpivot.tms.service.impl;

import cn.org.starpivot.api.mall.order.OrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderInternalDto;
import cn.org.starpivot.api.mall.ware.WareInternalClient;
import cn.org.starpivot.api.mall.ware.dto.OrderTaskTrackingRequest;
import cn.org.starpivot.api.tms.dto.InternalOrderDeliverRequest;
import cn.org.starpivot.api.tms.vo.ShipmentTrackingVo;
import cn.org.starpivot.api.tms.vo.TrackEventVo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.tms.config.TmsProperties;
import cn.org.starpivot.tms.constant.TmsConstants;
import cn.org.starpivot.tms.domain.dto.TmsShipmentQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsShipmentShipDto;
import cn.org.starpivot.tms.domain.entity.TmsShipment;
import cn.org.starpivot.tms.domain.entity.TmsTrackEvent;
import cn.org.starpivot.tms.domain.vo.TmsCarrierVo;
import cn.org.starpivot.tms.domain.vo.TmsShipmentVo;
import cn.org.starpivot.tms.domain.vo.TmsTrackEventVo;
import cn.org.starpivot.tms.integration.Kuaidi100Client;
import cn.org.starpivot.tms.mapper.TmsShipmentMapper;
import cn.org.starpivot.tms.mapper.TmsTrackEventMapper;
import cn.org.starpivot.tms.service.TmsCarrierService;
import cn.org.starpivot.tms.service.TmsNotificationPublisher;
import cn.org.starpivot.tms.service.TmsShipmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsShipmentServiceImpl implements TmsShipmentService {

    private static final int ORDER_STATUS_WAIT_DELIVER = 1;

    private final TmsShipmentMapper shipmentMapper;
    private final TmsTrackEventMapper trackEventMapper;
    private final TmsCarrierService carrierService;
    private final OrderInternalClient orderInternalClient;
    private final WareInternalClient wareInternalClient;
    private final Kuaidi100Client kuaidi100Client;
    private final TmsNotificationPublisher notificationPublisher;
    private final TmsProperties tmsProperties;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TmsShipmentVo> pageList(TmsShipmentQueryDto query) {
        Page<TmsShipment> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TmsShipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getOrderSn()), TmsShipment::getOrderSn, query.getOrderSn())
                .like(StringUtils.hasText(query.getTrackingNo()), TmsShipment::getTrackingNo, query.getTrackingNo())
                .like(StringUtils.hasText(query.getCarrierName()), TmsShipment::getCarrierName, query.getCarrierName())
                .eq(StringUtils.hasText(query.getStatus()), TmsShipment::getStatus, query.getStatus())
                .orderByDesc(TmsShipment::getId);
        Page<TmsShipment> result = shipmentMapper.selectPage(page, wrapper);
        PageResponse<TmsShipmentVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVoWithoutEvents).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TmsShipmentVo getDetail(Long shipmentId) {
        TmsShipment shipment = requireShipment(shipmentId);
        TmsShipmentVo vo = toVoWithoutEvents(shipment);
        vo.setEvents(loadTrackEvents(shipmentId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ship(TmsShipmentShipDto dto) {
        TmsCarrierVo carrier = carrierService.requireEnabledCarrier(dto.getCarrierId());
        OrderInternalDto order = requireWaitDeliverOrder(dto.getOrderId());
        ensureNoExistingShipment(order.getId());

        LocalDateTime now = LocalDateTime.now();
        TmsShipment shipment = new TmsShipment();
        shipment.setShipmentSn(generateShipmentSn());
        shipment.setBizModule(TmsConstants.BIZ_MODULE_MALL);
        shipment.setBizType(TmsConstants.BIZ_TYPE_ORDER);
        shipment.setBizId(order.getId());
        shipment.setBizKey(TmsConstants.bizKey(order.getId()));
        shipment.setOrderSn(order.getOrderSn());
        shipment.setCarrierId(carrier.getId());
        shipment.setCarrierName(carrier.getCarrierName());
        shipment.setKuaidi100Com(carrier.getKuaidi100Com());
        shipment.setTrackingNo(dto.getTrackingNo().trim());
        shipment.setStatus(TmsConstants.STATUS_SHIPPED);
        shipment.setReceiverName(order.getReceiverName());
        shipment.setReceiverPhone(order.getReceiverPhone());
        shipment.setReceiverAddress(buildAddress(order));
        shipment.setShipTime(now);
        shipment.setCreateTime(now);
        shipment.setUpdateTime(now);
        shipmentMapper.insert(shipment);

        saveSystemEvent(shipment.getId(), now, "卖家已发货，等待承运商揽收");

        InternalOrderDeliverRequest deliverRequest = new InternalOrderDeliverRequest();
        deliverRequest.setOrderId(order.getId());
        deliverRequest.setDeliveryCompany(carrier.getCarrierName());
        deliverRequest.setDeliverySn(shipment.getTrackingNo());
        Result<Void> deliverResult = orderInternalClient.syncDeliver(deliverRequest);
        if (deliverResult == null || !deliverResult.isSuccess()) {
            throw new BizException(deliverResult != null ? deliverResult.getMessage() : "同步订单发货失败");
        }

        syncWmsTracking(order.getId(), shipment.getTrackingNo());

        if (kuaidi100Client.isEnabled()) {
            syncTrackFromKuaidi100(shipment);
        }

        notificationPublisher.notifyOrderShipped(order.getOrderSn(), carrier.getCarrierName(), shipment.getTrackingNo());
        return shipment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingVo getTracking(String bizModule, String bizType, Long bizId) {
        TmsShipment shipment = findByBiz(bizModule, bizType, bizId);
        if (shipment == null) {
            return null;
        }
        return toTrackingVo(shipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentTrackingVo refreshTrack(Long shipmentId) {
        TmsShipment shipment = requireShipment(shipmentId);
        if (kuaidi100Client.isEnabled()) {
            syncTrackFromKuaidi100(shipment);
            shipment = requireShipment(shipmentId);
        }
        return toTrackingVo(shipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshPendingTracks(int batchSize) {
        if (!kuaidi100Client.isEnabled() || batchSize <= 0) {
            return;
        }
        List<TmsShipment> pending = shipmentMapper.selectList(new LambdaQueryWrapper<TmsShipment>()
                .in(TmsShipment::getStatus, TmsConstants.STATUS_SHIPPED, TmsConstants.STATUS_IN_TRANSIT)
                .orderByAsc(TmsShipment::getUpdateTime)
                .last("LIMIT " + batchSize));
        for (TmsShipment shipment : pending) {
            try {
                syncTrackFromKuaidi100(shipment);
            } catch (Exception ex) {
                log.warn("Scheduled track refresh failed, shipmentId={}", shipment.getId(), ex);
            }
        }
    }

    private void syncWmsTracking(Long orderId, String trackingNo) {
        try {
            OrderTaskTrackingRequest request = new OrderTaskTrackingRequest();
            request.setOrderId(orderId);
            request.setTrackingNo(trackingNo);
            Result<Void> result = wareInternalClient.updateTrackingByOrderId(request);
            if (result == null || !result.isSuccess()) {
                log.warn(
                        "WMS tracking sync failed, orderId={}, msg={}",
                        orderId,
                        result != null ? result.getMessage() : "null");
            }
        } catch (Exception ex) {
            log.warn("WMS tracking sync error, orderId={}", orderId, ex);
        }
    }

    private void syncTrackFromKuaidi100(TmsShipment shipment) {
        if (!StringUtils.hasText(shipment.getKuaidi100Com()) || !StringUtils.hasText(shipment.getTrackingNo())) {
            return;
        }
        Kuaidi100Client.QueryResult queryResult =
                kuaidi100Client.queryTrack(shipment.getKuaidi100Com(), shipment.getTrackingNo());
        trackEventMapper.delete(new LambdaQueryWrapper<TmsTrackEvent>()
                .eq(TmsTrackEvent::getShipmentId, shipment.getId())
                .eq(TmsTrackEvent::getSource, TmsConstants.EVENT_SOURCE_KUAIDI100));

        for (Kuaidi100Client.TrackItem item : queryResult.items()) {
            TmsTrackEvent event = new TmsTrackEvent();
            event.setShipmentId(shipment.getId());
            event.setEventTime(item.getEventTime());
            event.setEventDesc(item.getEventDesc());
            event.setLocation(item.getLocation());
            event.setSource(TmsConstants.EVENT_SOURCE_KUAIDI100);
            event.setRawJson(item.getRawJson());
            event.setCreateTime(LocalDateTime.now());
            trackEventMapper.insert(event);
        }

        String mappedStatus = kuaidi100Client.resolveShipmentStatus(queryResult.state());
        TmsShipment patch = new TmsShipment();
        patch.setId(shipment.getId());
        patch.setUpdateTime(LocalDateTime.now());
        if (StringUtils.hasText(mappedStatus)) {
            patch.setStatus(mappedStatus);
            if (TmsConstants.STATUS_DELIVERED.equals(mappedStatus)) {
                patch.setDeliverTime(LocalDateTime.now());
            }
        } else if (!queryResult.items().isEmpty()) {
            patch.setStatus(TmsConstants.STATUS_IN_TRANSIT);
        }
        shipmentMapper.updateById(patch);

        if (TmsConstants.STATUS_DELIVERED.equals(mappedStatus)) {
            tryAutoConfirmReceive(shipment);
        }
    }

    private void tryAutoConfirmReceive(TmsShipment shipment) {
        if (!tmsProperties.isAutoConfirmOnDelivered()) {
            return;
        }
        if (!TmsConstants.BIZ_MODULE_MALL.equals(shipment.getBizModule())
                || !TmsConstants.BIZ_TYPE_ORDER.equals(shipment.getBizType())) {
            return;
        }
        try {
            Result<Void> result = orderInternalClient.syncConfirmReceive(shipment.getBizId());
            if (result != null && result.isSuccess()) {
                saveSystemEvent(
                        shipment.getId(),
                        LocalDateTime.now(),
                        "快件已签收，系统已自动确认收货");
                log.info("Auto confirm receive success, orderId={}", shipment.getBizId());
            } else {
                log.debug(
                        "Auto confirm receive skipped, orderId={}, msg={}",
                        shipment.getBizId(),
                        result != null ? result.getMessage() : "null");
            }
        } catch (Exception ex) {
            log.warn("Auto confirm receive failed, orderId={}", shipment.getBizId(), ex);
        }
    }

    private OrderInternalDto requireWaitDeliverOrder(Long orderId) {
        Result<OrderInternalDto> result = orderInternalClient.getOrderSummary(orderId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : "订单不存在");
        }
        OrderInternalDto order = result.getData();
        if (!Integer.valueOf(ORDER_STATUS_WAIT_DELIVER).equals(order.getStatus())) {
            throw new BizException("仅待发货订单可执行发货操作");
        }
        return order;
    }

    private void ensureNoExistingShipment(Long orderId) {
        TmsShipment existing = findByBiz(TmsConstants.BIZ_MODULE_MALL, TmsConstants.BIZ_TYPE_ORDER, orderId);
        if (existing != null) {
            throw new BizException("该订单已创建运单，请勿重复发货");
        }
    }

    private TmsShipment findByBiz(String bizModule, String bizType, Long bizId) {
        return shipmentMapper.selectOne(new LambdaQueryWrapper<TmsShipment>()
                .eq(TmsShipment::getBizModule, bizModule)
                .eq(TmsShipment::getBizType, bizType)
                .eq(TmsShipment::getBizId, bizId)
                .last("LIMIT 1"));
    }

    private TmsShipment requireShipment(Long shipmentId) {
        TmsShipment shipment = shipmentMapper.selectById(shipmentId);
        if (shipment == null) {
            throw new BizException("运单不存在");
        }
        return shipment;
    }

    private void saveSystemEvent(Long shipmentId, LocalDateTime eventTime, String desc) {
        TmsTrackEvent event = new TmsTrackEvent();
        event.setShipmentId(shipmentId);
        event.setEventTime(eventTime);
        event.setEventStatus(TmsConstants.STATUS_SHIPPED);
        event.setEventDesc(desc);
        event.setSource(TmsConstants.EVENT_SOURCE_SYSTEM);
        event.setCreateTime(LocalDateTime.now());
        trackEventMapper.insert(event);
    }

    private List<TmsTrackEventVo> loadTrackEvents(Long shipmentId) {
        return trackEventMapper.selectList(new LambdaQueryWrapper<TmsTrackEvent>()
                        .eq(TmsTrackEvent::getShipmentId, shipmentId)
                        .orderByDesc(TmsTrackEvent::getEventTime)
                        .orderByDesc(TmsTrackEvent::getId))
                .stream()
                .map(this::toTrackVo)
                .collect(Collectors.toList());
    }

    private ShipmentTrackingVo toTrackingVo(TmsShipment shipment) {
        ShipmentTrackingVo vo = new ShipmentTrackingVo();
        vo.setShipmentId(shipment.getId());
        vo.setShipmentSn(shipment.getShipmentSn());
        vo.setOrderSn(shipment.getOrderSn());
        vo.setCarrierName(shipment.getCarrierName());
        vo.setKuaidi100Com(shipment.getKuaidi100Com());
        vo.setTrackingNo(shipment.getTrackingNo());
        vo.setStatus(shipment.getStatus());
        vo.setShipTime(shipment.getShipTime());
        vo.setDeliverTime(shipment.getDeliverTime());
        List<TrackEventVo> events = loadTrackEvents(shipment.getId()).stream()
                .sorted(Comparator.comparing(TmsTrackEventVo::getEventTime).reversed())
                .map(this::toApiTrackVo)
                .collect(Collectors.toList());
        vo.setEvents(events);
        return vo;
    }

    private TrackEventVo toApiTrackVo(TmsTrackEventVo source) {
        TrackEventVo vo = new TrackEventVo();
        vo.setEventTime(source.getEventTime());
        vo.setEventStatus(source.getEventStatus());
        vo.setEventDesc(source.getEventDesc());
        vo.setLocation(source.getLocation());
        vo.setSource(source.getSource());
        return vo;
    }

    private TmsTrackEventVo toTrackVo(TmsTrackEvent entity) {
        TmsTrackEventVo vo = new TmsTrackEventVo();
        vo.setEventTime(entity.getEventTime());
        vo.setEventStatus(entity.getEventStatus());
        vo.setEventDesc(entity.getEventDesc());
        vo.setLocation(entity.getLocation());
        vo.setSource(entity.getSource());
        return vo;
    }

    private TmsShipmentVo toVoWithoutEvents(TmsShipment entity) {
        TmsShipmentVo vo = new TmsShipmentVo();
        vo.setId(entity.getId());
        vo.setShipmentSn(entity.getShipmentSn());
        vo.setOrderSn(entity.getOrderSn());
        vo.setBizId(entity.getBizId());
        vo.setCarrierName(entity.getCarrierName());
        vo.setKuaidi100Com(entity.getKuaidi100Com());
        vo.setTrackingNo(entity.getTrackingNo());
        vo.setStatus(entity.getStatus());
        vo.setReceiverName(entity.getReceiverName());
        vo.setReceiverPhone(entity.getReceiverPhone());
        vo.setReceiverAddress(entity.getReceiverAddress());
        vo.setShipTime(entity.getShipTime());
        vo.setDeliverTime(entity.getDeliverTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String buildAddress(OrderInternalDto order) {
        return String.join(
                "",
                nullToEmpty(order.getReceiverProvince()),
                nullToEmpty(order.getReceiverCity()),
                nullToEmpty(order.getReceiverRegion()),
                nullToEmpty(order.getReceiverDetailAddress()));
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private String generateShipmentSn() {
        return "TMS" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
