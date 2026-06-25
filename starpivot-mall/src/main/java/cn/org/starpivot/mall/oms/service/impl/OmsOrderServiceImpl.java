package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.oms.domain.bo.OmsDeliverBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderCloseBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderItemVo;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderService;
import cn.org.starpivot.mall.oms.service.OmsOrderStockService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类。
 * <p>
 * 实现 {@link OmsOrderService}，处理订单相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see OmsOrderService
 */

@Service
@RequiredArgsConstructor
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OmsOrderService {

    private static final int STATUS_WAIT_DELIVER = 1;
    private static final int STATUS_DELIVERED = 2;
    private static final int STATUS_CLOSED = 4;

    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final PortalStockLockService portalStockLockService;
    private final OmsOrderStockService omsOrderStockService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OmsOrderVo> pageList(OmsOrderReqBo reqBo) {
        Page<OmsOrder> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<OmsOrder> orderPage = baseMapper.selectPageList(page, reqBo);

        List<OmsOrderVo> rows = orderPage.getRecords().stream()
                .map(this::toVoWithoutItems)
                .collect(Collectors.toList());

        PageResponse<OmsOrderVo> response = new PageResponse<>();
        response.setTotal(orderPage.getTotal());
        response.setRows(rows);
        response.setPageNum(orderPage.getCurrent());
        response.setPageSize(orderPage.getSize());
        response.setPageCount(orderPage.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OmsOrderVo getDetailById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        OmsOrder order = baseMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus())) {
            throw new BizException("订单不存在");
        }
        OmsOrderVo vo = toVoWithoutItems(order);
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, id));
        vo.setOrderItemList(items.stream().map(this::toItemVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliver(OmsDeliverBo bo) {
        OmsOrder order = requireOrder(bo.getOrderId());
        if (!Integer.valueOf(STATUS_WAIT_DELIVER).equals(order.getStatus())) {
            throw new BizException("仅待发货订单可执行发货操作");
        }
        order.setStatus(STATUS_DELIVERED);
        order.setDeliveryCompany(bo.getDeliveryCompany());
        order.setDeliverySn(bo.getDeliverySn());
        order.setDeliveryTime(LocalDateTime.now());
        order.setModifyTime(LocalDateTime.now());
        baseMapper.updateById(order);
        saveOperateHistory(order.getId(), STATUS_DELIVERED, "订单发货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(OmsOrderCloseBo bo) {
        OmsOrder order = requireOrder(bo.getOrderId());
        if (Integer.valueOf(STATUS_CLOSED).equals(order.getStatus())) {
            throw new BizException("订单已关闭");
        }
        if (Integer.valueOf(3).equals(order.getStatus()) || Integer.valueOf(5).equals(order.getStatus())) {
            throw new BizException("当前订单状态不可关闭");
        }
        Integer previousStatus = order.getStatus();
        order.setStatus(STATUS_CLOSED);
        order.setModifyTime(LocalDateTime.now());
        baseMapper.updateById(order);
        if (Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(previousStatus)) {
            portalStockLockService.releaseForOrder(order.getOrderSn());
        } else if (Integer.valueOf(STATUS_WAIT_DELIVER).equals(previousStatus)) {
            omsOrderStockService.restoreStockForOrder(order.getId());
            saveOperateHistory(order.getId(), STATUS_CLOSED, "关闭订单（已回滚库存与销量）");
            return;
        }
        saveOperateHistory(order.getId(), STATUS_CLOSED, "关闭订单");
    }

    private OmsOrder requireOrder(Long orderId) {
        if (orderId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        OmsOrder order = baseMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus())) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private void saveOperateHistory(Long orderId, Integer orderStatus, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(SecurityContextUtils.getUsername());
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(orderStatus);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }

    private OmsOrderVo toVoWithoutItems(OmsOrder order) {
        OmsOrderVo vo = new OmsOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private OmsOrderItemVo toItemVo(OmsOrderItem item) {
        OmsOrderItemVo vo = new OmsOrderItemVo();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
}
