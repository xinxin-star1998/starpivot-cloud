package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款信息服务实现类。
 * <p>
 * 实现 {@link OmsRefundInfoService}，处理退款信息相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see OmsRefundInfoService
 */

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

        List<RefundVo> rows = refundPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());

        PageResponse<RefundVo> response = new PageResponse<>();
        response.setTotal(refundPage.getTotal());
        response.setRows(rows);
        response.setPageNum(refundPage.getCurrent());
        response.setPageSize(refundPage.getSize());
        response.setPageCount(refundPage.getPages());
        return response;
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

    private PageResponse<RefundVo> emptyPage(RefundReqBo reqBo) {
        PageResponse<RefundVo> response = new PageResponse<>();
        response.setTotal(0L);
        response.setRows(Collections.emptyList());
        response.setPageNum((long) reqBo.getPageNum());
        response.setPageSize((long) reqBo.getPageSize());
        response.setPageCount(0L);
        return response;
    }

    private RefundVo toVo(OmsRefundInfo entity) {
        RefundVo vo = new RefundVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
