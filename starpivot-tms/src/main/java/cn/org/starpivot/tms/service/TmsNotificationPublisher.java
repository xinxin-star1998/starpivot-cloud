package cn.org.starpivot.tms.service;

import cn.org.starpivot.api.system.SysMessageClient;
import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.api.system.dto.MessageSendToRolesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmsNotificationPublisher {

    private final SysMessageClient sysMessageClient;

    public void notifyOrderShipped(String orderSn, String carrierName, String trackingNo) {
        try {
            MessageSendToRolesRequest request = new MessageSendToRolesRequest();
            request.setRoleKeys(List.of("admin"));
            request.setMsgType(MessageConstants.MSG_TYPE_ORDER);
            request.setTitle("订单已发货");
            request.setContent("订单 " + orderSn + " 已通过 " + carrierName + " 发出，运单号 " + trackingNo);
            request.setBizModule(MessageConstants.BIZ_MODULE_MALL);
            request.setBizType("order");
            request.setBizKey("mall:order:ship:" + orderSn);
            request.setLinkPath("/tms/shipment");
            sysMessageClient.sendToRoles(request);
        } catch (Exception ex) {
            log.warn("Ship notification failed, orderSn={}", orderSn, ex);
        }
    }
}
