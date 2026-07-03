package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.PaymentReqBo;
import cn.org.starpivot.mall.oms.domain.vo.PaymentVo;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import cn.org.starpivot.mall.oms.mapper.OmsPaymentInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsPaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付信息服务实现类。
 * <p>
 * 实现 {@link OmsPaymentInfoService}，处理支付信息相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 * </ul>
 *
 * @see OmsPaymentInfoService
 */

@Service
public class OmsPaymentInfoServiceImpl extends ServiceImpl<OmsPaymentInfoMapper, OmsPaymentInfo>
        implements OmsPaymentInfoService {

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentVo> pageList(PaymentReqBo reqBo) {
        Page<OmsPaymentInfo> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<OmsPaymentInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reqBo.getOrderSn())) {
            wrapper.like(OmsPaymentInfo::getOrderSn, reqBo.getOrderSn());
        }
        wrapper.orderByDesc(OmsPaymentInfo::getId);
        IPage<OmsPaymentInfo> paymentPage = baseMapper.selectPage(page, wrapper);

        List<PaymentVo> rows = paymentPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());

        PageResponse<PaymentVo> response = new PageResponse<>();
        response.setTotal(paymentPage.getTotal());
        response.setRows(rows);
        response.setPageNum(paymentPage.getCurrent());
        response.setPageSize(paymentPage.getSize());
        response.setPageCount(paymentPage.getPages());
        return response;
    }

    private PaymentVo toVo(OmsPaymentInfo entity) {
        PaymentVo vo = new PaymentVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
