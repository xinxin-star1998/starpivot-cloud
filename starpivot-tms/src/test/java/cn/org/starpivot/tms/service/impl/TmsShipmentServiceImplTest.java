package cn.org.starpivot.tms.service.impl;

import cn.org.starpivot.api.mall.order.OrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderInternalDto;
import cn.org.starpivot.api.mall.ware.WareInternalClient;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.tms.config.TmsProperties;
import cn.org.starpivot.tms.constant.TmsConstants;
import cn.org.starpivot.tms.domain.dto.TmsShipmentShipDto;
import cn.org.starpivot.tms.domain.entity.TmsShipment;
import cn.org.starpivot.tms.domain.entity.TmsTrackEvent;
import cn.org.starpivot.tms.domain.vo.TmsCarrierVo;
import cn.org.starpivot.tms.domain.vo.TmsShipmentVo;
import cn.org.starpivot.tms.integration.Kuaidi100Client;
import cn.org.starpivot.tms.mapper.TmsShipmentMapper;
import cn.org.starpivot.tms.mapper.TmsTrackEventMapper;
import cn.org.starpivot.tms.service.TmsCarrierService;
import cn.org.starpivot.tms.service.TmsNotificationPublisher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link TmsShipmentServiceImpl} 单元测试。
 * <p>覆盖发货、运单详情查询、物流追踪等核心场景。</p>
 */
@ExtendWith(MockitoExtension.class)
class TmsShipmentServiceImplTest {

    @Mock private TmsShipmentMapper shipmentMapper;
    @Mock private TmsTrackEventMapper trackEventMapper;
    @Mock private TmsCarrierService carrierService;
    @Mock private OrderInternalClient orderInternalClient;
    @Mock private WareInternalClient wareInternalClient;
    @Mock private Kuaidi100Client kuaidi100Client;
    @Mock private TmsNotificationPublisher notificationPublisher;
    @Mock private TmsProperties tmsProperties;

    @InjectMocks
    private TmsShipmentServiceImpl tmsShipmentService;

    // ==================== getDetail ====================

    @Nested
    @DisplayName("getDetail 运单详情")
    class GetDetailTests {

        @Test
        @DisplayName("运单不存在时抛出 BizException")
        void shipmentNotFound_throwsException() {
            when(shipmentMapper.selectById(999L)).thenReturn(null);

            assertThrows(BizException.class, () -> tmsShipmentService.getDetail(999L));
        }

        @Test
        @DisplayName("正常返回运单详情（含事件列表）")
        void normalReturn_withEvents() {
            TmsShipment shipment = buildShipment();
            when(shipmentMapper.selectById(1L)).thenReturn(shipment);
            when(trackEventMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            TmsShipmentVo vo = tmsShipmentService.getDetail(1L);

            assertNotNull(vo);
            assertEquals("SN001", vo.getShipmentSn());
            assertEquals("ORDER001", vo.getOrderSn());
            assertNotNull(vo.getEvents());
        }
    }

    // ==================== ship ====================

    @Nested
    @DisplayName("ship 发货操作")
    class ShipTests {

        @Test
        @DisplayName("正常发货成功")
        void normalShip_succeeds() {
            TmsShipmentShipDto dto = new TmsShipmentShipDto();
            dto.setCarrierId(1L);
            dto.setOrderId(100L);
            dto.setTrackingNo("SF123456");

            // 承运商
            TmsCarrierVo carrier = new TmsCarrierVo();
            carrier.setId(1L);
            carrier.setCarrierName("顺丰");
            carrier.setKuaidi100Com("shunfeng");
            when(carrierService.requireEnabledCarrier(1L)).thenReturn(carrier);

            // 订单
            OrderInternalDto order = new OrderInternalDto();
            order.setId(100L);
            order.setOrderSn("ORD001");
            order.setStatus(1);
            order.setReceiverName("张三");
            order.setReceiverPhone("13800000000");
            order.setReceiverProvince("浙江省");
            order.setReceiverCity("杭州市");
            order.setReceiverRegion("西湖区");
            order.setReceiverDetailAddress("XX路XX号");
            when(orderInternalClient.getOrderSummary(100L))
                    .thenReturn(Result.success(order));

            // 无已有运单
            when(shipmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // 插入运单
            when(shipmentMapper.insert(any(TmsShipment.class))).thenAnswer(invocation -> {
                TmsShipment s = invocation.getArgument(0);
                s.setId(50L);
                return 1;
            });

            // 同步订单状态
            when(orderInternalClient.syncDeliver(any())).thenReturn(Result.success());
            // WMS 同步
            when(wareInternalClient.updateTrackingByOrderId(any())).thenReturn(Result.success());

            Long shipmentId = tmsShipmentService.ship(dto);

            assertEquals(50L, shipmentId);
            verify(trackEventMapper).insert(any(TmsTrackEvent.class));
            verify(orderInternalClient).syncDeliver(any());
            verify(notificationPublisher).notifyOrderShipped(eq("ORD001"), eq("顺丰"), eq("SF123456"));
        }

        @Test
        @DisplayName("订单已存在运单时抛出异常（重复发货）")
        void duplicateShipment_throwsException() {
            TmsShipmentShipDto dto = new TmsShipmentShipDto();
            dto.setCarrierId(1L);
            dto.setOrderId(100L);
            dto.setTrackingNo("SF123456");

            TmsCarrierVo carrier = new TmsCarrierVo();
            carrier.setId(1L);
            carrier.setCarrierName("顺丰");
            when(carrierService.requireEnabledCarrier(1L)).thenReturn(carrier);

            OrderInternalDto order = new OrderInternalDto();
            order.setId(100L);
            order.setStatus(1);
            when(orderInternalClient.getOrderSummary(100L)).thenReturn(Result.success(order));

            // 已有运单
            TmsShipment existing = new TmsShipment();
            existing.setId(50L);
            when(shipmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            BizException ex = assertThrows(BizException.class, () -> tmsShipmentService.ship(dto));
            assertTrue(ex.getMessage().contains("重复"));
        }

        @Test
        @DisplayName("订单非待发货状态时抛出异常")
        void orderNotWaitDeliver_throwsException() {
            TmsShipmentShipDto dto = new TmsShipmentShipDto();
            dto.setCarrierId(1L);
            dto.setOrderId(100L);
            dto.setTrackingNo("SF123456");

            TmsCarrierVo carrier = new TmsCarrierVo();
            carrier.setId(1L);
            when(carrierService.requireEnabledCarrier(1L)).thenReturn(carrier);

            OrderInternalDto order = new OrderInternalDto();
            order.setId(100L);
            order.setStatus(2); // 非待发货
            when(orderInternalClient.getOrderSummary(100L)).thenReturn(Result.success(order));

            BizException ex = assertThrows(BizException.class, () -> tmsShipmentService.ship(dto));
            assertTrue(ex.getMessage().contains("待发货"));
        }

        @Test
        @DisplayName("订单不存在时抛出异常")
        void orderNotFound_throwsException() {
            TmsShipmentShipDto dto = new TmsShipmentShipDto();
            dto.setCarrierId(1L);
            dto.setOrderId(999L);
            dto.setTrackingNo("SF123456");

            TmsCarrierVo carrier = new TmsCarrierVo();
            carrier.setId(1L);
            when(carrierService.requireEnabledCarrier(1L)).thenReturn(carrier);

            when(orderInternalClient.getOrderSummary(999L)).thenReturn(Result.notFound("订单不存在"));

            assertThrows(BizException.class, () -> tmsShipmentService.ship(dto));
        }
    }

    // ==================== getTracking ====================

    @Nested
    @DisplayName("getTracking 物流追踪")
    class GetTrackingTests {

        @Test
        @DisplayName("无运单记录时返回 null")
        void noShipment_returnsNull() {
            when(shipmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            var result = tmsShipmentService.getTracking("mall", "order", 999L);
            assertNull(result);
        }

        @Test
        @DisplayName("正常返回物流追踪信息")
        void normalReturn_succeeds() {
            TmsShipment shipment = buildShipment();
            when(shipmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shipment);
            when(trackEventMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            var result = tmsShipmentService.getTracking("mall", "order", 100L);

            assertNotNull(result);
            assertEquals("SN001", result.getShipmentSn());
            assertEquals(TmsConstants.STATUS_SHIPPED, result.getStatus());
        }
    }

    // ==================== refreshPendingTracks ====================

    @Nested
    @DisplayName("refreshPendingTracks 批量刷新")
    class RefreshPendingTests {

        @Test
        @DisplayName("快递100未启用时直接返回")
        void kuaidi100Disabled_returnsEarly() {
            when(kuaidi100Client.isEnabled()).thenReturn(false);

            tmsShipmentService.refreshPendingTracks(100);

            verify(shipmentMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("batchSize 为 0 时直接返回")
        void batchSizeZero_returnsEarly() {
            tmsShipmentService.refreshPendingTracks(0);

            verify(shipmentMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== helper ====================

    private TmsShipment buildShipment() {
        TmsShipment shipment = new TmsShipment();
        shipment.setId(1L);
        shipment.setShipmentSn("SN001");
        shipment.setOrderSn("ORDER001");
        shipment.setBizModule(TmsConstants.BIZ_MODULE_MALL);
        shipment.setBizType(TmsConstants.BIZ_TYPE_ORDER);
        shipment.setBizId(100L);
        shipment.setCarrierName("顺丰");
        shipment.setKuaidi100Com("shunfeng");
        shipment.setTrackingNo("SF123456");
        shipment.setStatus(TmsConstants.STATUS_SHIPPED);
        shipment.setShipTime(LocalDateTime.now());
        shipment.setCreateTime(LocalDateTime.now());
        return shipment;
    }
}
