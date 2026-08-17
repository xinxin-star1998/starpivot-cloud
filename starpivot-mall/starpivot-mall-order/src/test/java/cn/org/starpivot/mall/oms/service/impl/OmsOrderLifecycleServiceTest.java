package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.entity.OmsOrderSetting;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OmsOrderLifecycleServiceTest {

    @Mock
    private OmsOrderMapper omsOrderMapper;
    @Mock
    private OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    @Mock
    private OmsOrderSettingService omsOrderSettingService;

    @InjectMocks
    private OmsOrderLifecycleService lifecycleService;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, OmsOrder.class);
        TableInfoHelper.initTableInfo(assistant, OmsOrderOperateHistory.class);
    }

    @Nested
    class ConfirmReceive {

        @Test
        void shouldConfirmDeliveredOrder() {
            OmsOrder order = buildOrder(1L, PortalConstants.ORDER_STATUS_DELIVERED);
            when(omsOrderMapper.selectById(1L)).thenReturn(order);
            when(omsOrderMapper.updateById(any(OmsOrder.class))).thenReturn(1);
            when(omsOrderOperateHistoryMapper.insert(any(OmsOrderOperateHistory.class))).thenReturn(1);

            lifecycleService.confirmReceive(1L, "member_1");

            ArgumentCaptor<OmsOrder> captor = ArgumentCaptor.forClass(OmsOrder.class);
            verify(omsOrderMapper).updateById(captor.capture());
            assertEquals(PortalConstants.ORDER_STATUS_COMPLETED, captor.getValue().getStatus());
            assertEquals(1, captor.getValue().getConfirmStatus());
            assertNotNull(captor.getValue().getReceiveTime());

            verify(omsOrderOperateHistoryMapper).insert(any(OmsOrderOperateHistory.class));
        }

        @Test
        void shouldThrowForNonDeliveredOrder() {
            OmsOrder order = buildOrder(2L, PortalConstants.ORDER_STATUS_UNPAID);
            when(omsOrderMapper.selectById(2L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class,
                    () -> lifecycleService.confirmReceive(2L, "member_1"));
            assertEquals("仅已发货订单可确认收货", ex.getMessage());
        }

        @Test
        void shouldThrowForDeletedOrder() {
            OmsOrder order = buildOrder(3L, PortalConstants.ORDER_STATUS_DELIVERED);
            order.setDeleteStatus(1);
            when(omsOrderMapper.selectById(3L)).thenReturn(order);

            assertThrows(BizException.class,
                    () -> lifecycleService.confirmReceive(3L, "member_1"));
        }

        @Test
        void shouldThrowWhenOrderNotFound() {
            when(omsOrderMapper.selectById(99L)).thenReturn(null);

            assertThrows(BizException.class,
                    () -> lifecycleService.confirmReceive(99L, "member_1"));
        }
    }

    @Nested
    class AutoConfirmDeliveredOrders {

        @Test
        void shouldReturnZeroWhenDaysNotConfigured() {
            OmsOrderSetting setting = new OmsOrderSetting();
            setting.setConfirmOvertime(null);
            when(omsOrderSettingService.getSetting()).thenReturn(setting);

            assertEquals(0, lifecycleService.autoConfirmDeliveredOrders());
            verify(omsOrderMapper, never()).selectList(any());
        }

        @Test
        void shouldReturnZeroWhenDaysIsZero() {
            OmsOrderSetting setting = new OmsOrderSetting();
            setting.setConfirmOvertime(0);
            when(omsOrderSettingService.getSetting()).thenReturn(setting);

            assertEquals(0, lifecycleService.autoConfirmDeliveredOrders());
        }

        @Test
        void shouldAutoConfirmExpiredOrders() {
            OmsOrderSetting setting = new OmsOrderSetting();
            setting.setConfirmOvertime(7);
            when(omsOrderSettingService.getSetting()).thenReturn(setting);

            OmsOrder expiredOrder = buildOrder(10L, PortalConstants.ORDER_STATUS_DELIVERED);
            expiredOrder.setDeliveryTime(LocalDateTime.now().minusDays(10));
            when(omsOrderMapper.selectList(any())).thenReturn(List.of(expiredOrder));
            when(omsOrderMapper.updateById(any(OmsOrder.class))).thenReturn(1);
            when(omsOrderOperateHistoryMapper.insert(any(OmsOrderOperateHistory.class))).thenReturn(1);

            int count = lifecycleService.autoConfirmDeliveredOrders();
            assertEquals(1, count);
        }

        @Test
        void shouldReturnZeroWhenNoExpiredOrders() {
            OmsOrderSetting setting = new OmsOrderSetting();
            setting.setConfirmOvertime(7);
            when(omsOrderSettingService.getSetting()).thenReturn(setting);
            when(omsOrderMapper.selectList(any())).thenReturn(Collections.emptyList());

            assertEquals(0, lifecycleService.autoConfirmDeliveredOrders());
        }
    }

    @Nested
    class AutoFinishCompletedOrders {

        @Test
        void shouldReturnZeroWhenDaysNotConfigured() {
            OmsOrderSetting setting = new OmsOrderSetting();
            setting.setFinishOvertime(null);
            when(omsOrderSettingService.getSetting()).thenReturn(setting);

            assertEquals(0, lifecycleService.autoFinishCompletedOrders());
        }
    }

    private OmsOrder buildOrder(Long id, int status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setStatus(status);
        order.setDeleteStatus(0);
        order.setMemberId(100L);
        order.setOrderSn("SP20260101001");
        return order;
    }
}
