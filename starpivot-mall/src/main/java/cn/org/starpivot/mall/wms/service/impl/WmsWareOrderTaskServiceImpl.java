package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.wms.domain.bo.WareOrderTaskReqBo;
import cn.org.starpivot.mall.wms.domain.dto.WmsStockDeductionLine;
import cn.org.starpivot.mall.wms.domain.vo.WareOrderTaskDetailVo;
import cn.org.starpivot.mall.wms.domain.vo.WareOrderTaskVo;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTaskDetail;
import cn.org.starpivot.mall.wms.enums.WmsTaskLockStatusEnum;
import cn.org.starpivot.mall.wms.enums.WmsTaskStatusEnum;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import cn.org.starpivot.mall.wms.service.WmsStockWarningService;
import cn.org.starpivot.mall.wms.service.WmsWareOrderTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 仓库工单服务实现类。
 * <p>
 * 实现 {@link WmsWareOrderTaskService}，处理仓库工单相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see WmsWareOrderTaskService
 */

@Service
@RequiredArgsConstructor
public class WmsWareOrderTaskServiceImpl extends ServiceImpl<WmsWareOrderTaskMapper, WmsWareOrderTask>
        implements WmsWareOrderTaskService {

    private final WmsWareOrderTaskDetailMapper taskDetailMapper;
    private final WmsWareSkuMapper wmsWareSkuMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final WmsStockWarningService wmsStockWarningService;

    private static final int ORDER_STATUS_WAIT_DELIVER = 1;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WareOrderTaskVo> pageList(WareOrderTaskReqBo reqBo) {
        Page<WmsWareOrderTask> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<WmsWareOrderTask> taskPage = baseMapper.selectPageList(page, reqBo);

        PageResponse<WareOrderTaskVo> response = new PageResponse<>();
        response.setTotal(taskPage.getTotal());
        response.setRows(taskPage.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        response.setPageNum(taskPage.getCurrent());
        response.setPageSize(taskPage.getSize());
        response.setPageCount(taskPage.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public WareOrderTaskVo getDetailById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "工作单ID不能为空");
        }
        WmsWareOrderTask task = baseMapper.selectById(id);
        if (task == null) {
            throw new BizException("库存工作单不存在");
        }
        WareOrderTaskVo vo = toVo(task);
        vo.setDetails(taskDetailMapper.listByTaskId(id).stream()
                .map(this::toDetailVo)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockStock(Long taskId) {
        WmsWareOrderTask task = requireTask(taskId);
        ensureNotAutoFinished(task);
        if (!Integer.valueOf(WmsTaskStatusEnum.NEW.getCode()).equals(task.getTaskStatus())
                && !Integer.valueOf(WmsTaskStatusEnum.PROCESSING.getCode()).equals(task.getTaskStatus())) {
            throw new BizException("当前工作单状态不可锁定库存");
        }

        List<WmsWareOrderTaskDetail> details = taskDetailMapper.listByTaskId(taskId);
        if (details.isEmpty()) {
            throw new BizException("工作单无明细，无法锁定库存");
        }

        for (WmsWareOrderTaskDetail detail : details) {
            if (Integer.valueOf(WmsTaskLockStatusEnum.LOCKED.getCode()).equals(detail.getLockStatus())
                    || Integer.valueOf(WmsTaskLockStatusEnum.DEDUCTED.getCode()).equals(detail.getLockStatus())) {
                continue;
            }
            Long wareId = lockDetailStock(task, detail);
            detail.setWareId(wareId);
            detail.setLockStatus(WmsTaskLockStatusEnum.LOCKED.getCode());
            taskDetailMapper.updateById(detail);
        }

        task.setTaskStatus(WmsTaskStatusEnum.PROCESSING.getCode());
        baseMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long taskId) {
        WmsWareOrderTask task = requireTask(taskId);
        ensureNotAutoFinished(task);
        List<WmsWareOrderTaskDetail> details = taskDetailMapper.listByTaskId(taskId);

        for (WmsWareOrderTaskDetail detail : details) {
            if (!Integer.valueOf(WmsTaskLockStatusEnum.LOCKED.getCode()).equals(detail.getLockStatus())) {
                throw new BizException("存在未锁定的明细，请先锁定库存");
            }
            int rows = wmsWareSkuMapper.deductSkuStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
            if (rows == 0) {
                throw new BizException("SKU[" + detail.getSkuId() + "]扣减库存失败");
            }
            detail.setLockStatus(WmsTaskLockStatusEnum.DEDUCTED.getCode());
            taskDetailMapper.updateById(detail);
            wmsStockWarningService.checkAndCreatePurchaseDemand(detail.getSkuId(), detail.getWareId());
        }

        task.setTaskStatus(WmsTaskStatusEnum.FINISHED.getCode());
        baseMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockStock(Long taskId) {
        WmsWareOrderTask task = requireTask(taskId);
        ensureNotAutoFinished(task);
        List<WmsWareOrderTaskDetail> details = taskDetailMapper.listByTaskId(taskId);

        for (WmsWareOrderTaskDetail detail : details) {
            if (!Integer.valueOf(WmsTaskLockStatusEnum.LOCKED.getCode()).equals(detail.getLockStatus())) {
                continue;
            }
            int rows = wmsWareSkuMapper.unlockSkuStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
            if (rows == 0) {
                throw new BizException("SKU[" + detail.getSkuId() + "]解锁库存失败");
            }
            detail.setLockStatus(WmsTaskLockStatusEnum.UNLOCKED.getCode());
            taskDetailMapper.updateById(detail);
        }

        task.setTaskStatus(WmsTaskStatusEnum.INVALID.getCode());
        baseMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOrder(Long orderId) {
        if (orderId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus())) {
            throw new BizException("订单不存在");
        }
        if (!Integer.valueOf(ORDER_STATUS_WAIT_DELIVER).equals(order.getStatus())) {
            throw new BizException("仅待发货订单可生成库存工作单");
        }
        long exists = baseMapper.selectCount(
                new LambdaQueryWrapper<WmsWareOrderTask>().eq(WmsWareOrderTask::getOrderId, orderId));
        if (exists > 0) {
            throw new BizException("该订单已存在库存工作单（支付成功订单已自动生成出库记录）");
        }

        WmsWareOrderTask task = buildTaskFromOrder(order);
        task.setTaskStatus(WmsTaskStatusEnum.NEW.getCode());
        baseMapper.insert(task);

        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        if (items.isEmpty()) {
            throw new BizException("订单无商品明细，无法生成工作单");
        }
        for (OmsOrderItem item : items) {
            WmsWareOrderTaskDetail detail = new WmsWareOrderTaskDetail();
            detail.setTaskId(task.getId());
            detail.setSkuId(item.getSkuId());
            detail.setSkuName(item.getSkuName());
            detail.setSkuNum(item.getSkuQuantity());
            taskDetailMapper.insert(detail);
        }
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinishedRecordForPaidOrder(Long orderId, List<WmsStockDeductionLine> deductions) {
        if (orderId == null) {
            return null;
        }
        WmsWareOrderTask existing = baseMapper.selectOne(
                new LambdaQueryWrapper<WmsWareOrderTask>()
                        .eq(WmsWareOrderTask::getOrderId, orderId)
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing.getId();
        }

        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus())) {
            return null;
        }

        WmsWareOrderTask task = buildTaskFromOrder(order);
        task.setTaskStatus(WmsTaskStatusEnum.FINISHED.getCode());
        task.setTaskComment("支付成功自动生成（库存已扣减，仅作发货记录）");
        task.setWareId(resolveTaskWareId(deductions));
        baseMapper.insert(task);

        if (!CollectionUtils.isEmpty(deductions)) {
            for (WmsStockDeductionLine line : deductions) {
                insertFinishedDetail(task.getId(), line);
            }
        } else {
            List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
            for (OmsOrderItem item : items) {
                WmsWareOrderTaskDetail detail = new WmsWareOrderTaskDetail();
                detail.setTaskId(task.getId());
                detail.setSkuId(item.getSkuId());
                detail.setSkuName(item.getSkuName());
                detail.setSkuNum(item.getSkuQuantity());
                detail.setLockStatus(WmsTaskLockStatusEnum.DEDUCTED.getCode());
                taskDetailMapper.insert(detail);
            }
        }
        return task.getId();
    }

    private WmsWareOrderTask buildTaskFromOrder(OmsOrder order) {
        WmsWareOrderTask task = new WmsWareOrderTask();
        task.setOrderId(order.getId());
        task.setOrderSn(order.getOrderSn());
        task.setConsignee(order.getReceiverName());
        task.setConsigneeTel(order.getReceiverPhone());
        task.setDeliveryAddress(buildAddress(order));
        task.setOrderComment(order.getNote());
        task.setPaymentWay(order.getPayType());
        task.setOrderBody("订单商品出库");
        task.setCreateTime(LocalDateTime.now());
        return task;
    }

    private Long resolveTaskWareId(List<WmsStockDeductionLine> deductions) {
        if (CollectionUtils.isEmpty(deductions)) {
            return null;
        }
        Set<Long> wareIds = deductions.stream()
                .map(WmsStockDeductionLine::getWareId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        return wareIds.size() == 1 ? wareIds.iterator().next() : null;
    }

    private void insertFinishedDetail(Long taskId, WmsStockDeductionLine line) {
        if (line == null || line.getSkuId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
            return;
        }
        WmsWareOrderTaskDetail detail = new WmsWareOrderTaskDetail();
        detail.setTaskId(taskId);
        detail.setSkuId(line.getSkuId());
        detail.setSkuName(line.getSkuName());
        detail.setSkuNum(line.getQuantity());
        detail.setWareId(line.getWareId());
        detail.setLockStatus(WmsTaskLockStatusEnum.DEDUCTED.getCode());
        taskDetailMapper.insert(detail);
    }

    private String buildAddress(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        if (order.getReceiverProvince() != null) {
            sb.append(order.getReceiverProvince());
        }
        if (order.getReceiverCity() != null) {
            sb.append(order.getReceiverCity());
        }
        if (order.getReceiverRegion() != null) {
            sb.append(order.getReceiverRegion());
        }
        if (order.getReceiverDetailAddress() != null) {
            sb.append(order.getReceiverDetailAddress());
        }
        return sb.toString();
    }

    private void ensureNotAutoFinished(WmsWareOrderTask task) {
        if (Integer.valueOf(WmsTaskStatusEnum.FINISHED.getCode()).equals(task.getTaskStatus())) {
            throw new BizException("工作单已完成（支付时已扣减库存），不可重复锁定/扣减");
        }
    }

    private WmsWareOrderTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "工作单ID不能为空");
        }
        WmsWareOrderTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("库存工作单不存在");
        }
        return task;
    }

    private Long lockDetailStock(WmsWareOrderTask task, WmsWareOrderTaskDetail detail) {
        Long wareId = detail.getWareId() != null ? detail.getWareId() : task.getWareId();
        if (wareId != null) {
            if (wmsWareSkuMapper.lockSkuStock(detail.getSkuId(), wareId, detail.getSkuNum()) > 0) {
                return wareId;
            }
            throw new BizException("SKU[" + detail.getSkuId() + "]在仓库[" + wareId + "]库存不足");
        }

        List<Long> wareIds = wmsWareSkuMapper.listWareIdHasStock(detail.getSkuId());
        if (wareIds == null || wareIds.isEmpty()) {
            throw new BizException("SKU[" + detail.getSkuId() + "]无可用库存仓库");
        }
        for (Long candidate : wareIds) {
            if (wmsWareSkuMapper.lockSkuStock(detail.getSkuId(), candidate, detail.getSkuNum()) > 0) {
                return candidate;
            }
        }
        throw new BizException("SKU[" + detail.getSkuId() + "]库存不足");
    }

    private WareOrderTaskVo toVo(WmsWareOrderTask task) {
        WareOrderTaskVo vo = new WareOrderTaskVo();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }

    private WareOrderTaskDetailVo toDetailVo(WmsWareOrderTaskDetail detail) {
        WareOrderTaskDetailVo vo = new WareOrderTaskDetailVo();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}
