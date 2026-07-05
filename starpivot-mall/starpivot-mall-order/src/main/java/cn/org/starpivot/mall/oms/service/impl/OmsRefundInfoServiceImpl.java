package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OmsRefundInfoServiceImpl extends ServiceImpl<OmsRefundInfoMapper, OmsRefundInfo>
        implements OmsRefundInfoService {

    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RefundVo> pageList(RefundReqBo reqBo) {
        Page<OmsRefundInfo> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<OmsRefundInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reqBo.getOrderSn())) {
            List<Long> returnIds = findReturnIdsByOrderSn(reqBo.getOrderSn());
            if (returnIds.isEmpty()) {
                return emptyPage(reqBo);
            }
            wrapper.in(OmsRefundInfo::getOrderReturnId, returnIds);
        }
        wrapper.orderByDesc(OmsRefundInfo::getId);
        IPage<OmsRefundInfo> refundPage = baseMapper.selectPage(page, wrapper);

        Map<Long, String> orderSnByReturnId = loadOrderSnMap(refundPage.getRecords());
        List<RefundVo> rows = refundPage.getRecords().stream()
                .map(entity -> toVo(entity, orderSnByReturnId))
                .collect(Collectors.toList());

        PageResponse<RefundVo> response = new PageResponse<>();
        response.setTotal(refundPage.getTotal());
        response.setRows(rows);
        response.setPageNum(refundPage.getCurrent());
        response.setPageSize(refundPage.getSize());
        response.setPageCount(refundPage.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RefundVo getDetailById(Long id) {
        OmsRefundInfo entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new BizException("退款记录不存在");
        }
        Map<Long, String> orderSnByReturnId = loadOrderSnMap(List.of(entity));
        return toVo(entity, orderSnByReturnId);
    }

    private List<Long> findReturnIdsByOrderSn(String orderSn) {
        List<OmsOrderReturnApply> applies = omsOrderReturnApplyMapper.selectList(
                new LambdaQueryWrapper<OmsOrderReturnApply>()
                        .like(OmsOrderReturnApply::getOrderSn, orderSn)
                        .select(OmsOrderReturnApply::getId));
        if (applies.isEmpty()) {
            return Collections.emptyList();
        }
        return applies.stream().map(OmsOrderReturnApply::getId).collect(Collectors.toList());
    }

    private Map<Long, String> loadOrderSnMap(List<OmsRefundInfo> records) {
        List<Long> returnIds = records.stream()
                .map(OmsRefundInfo::getOrderReturnId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (returnIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return omsOrderReturnApplyMapper.selectList(
                        Wrappers.<OmsOrderReturnApply>lambdaQuery()
                                .in(OmsOrderReturnApply::getId, returnIds)
                                .select(OmsOrderReturnApply::getId, OmsOrderReturnApply::getOrderSn))
                .stream()
                .collect(Collectors.toMap(
                        OmsOrderReturnApply::getId,
                        OmsOrderReturnApply::getOrderSn,
                        (left, right) -> left));
    }

    private PageResponse<RefundVo> emptyPage(RefundReqBo reqBo) {
        PageResponse<RefundVo> response = new PageResponse<>();
        response.setTotal(0L);
        response.setRows(Collections.emptyList());
        response.setPageNum((long) reqBo.getPageNum());
        response.setPageSize((long) reqBo.getPageSize());
        response.setPageCount(0L);
        return response;
    }

    private RefundVo toVo(OmsRefundInfo entity, Map<Long, String> orderSnByReturnId) {
        RefundVo vo = new RefundVo();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getOrderReturnId() != null) {
            vo.setOrderSn(orderSnByReturnId.get(entity.getOrderReturnId()));
        }
        return vo;
    }
}
