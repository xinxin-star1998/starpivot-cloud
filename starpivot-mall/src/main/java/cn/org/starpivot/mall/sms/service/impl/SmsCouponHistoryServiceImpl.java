package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponHistoryReqBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponHistoryVo;
import cn.org.starpivot.mall.sms.entity.SmsCouponHistory;
import cn.org.starpivot.mall.sms.mapper.SmsCouponHistoryMapper;
import cn.org.starpivot.mall.sms.service.SmsCouponHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 优惠券领取历史服务实现类。
 * <p>
 * 实现 {@link SmsCouponHistoryService}，处理优惠券领取历史相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsCouponHistoryService
 */

@Service
@RequiredArgsConstructor
public class SmsCouponHistoryServiceImpl implements SmsCouponHistoryService {

    private final SmsCouponHistoryMapper smsCouponHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponHistoryVo> pageList(CouponHistoryReqBo reqBo) {
        Page<SmsCouponHistory> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsCouponHistory> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getMemberId() != null) {
            wrapper.eq(SmsCouponHistory::getMemberId, reqBo.getMemberId());
        }
        if (reqBo.getCouponId() != null) {
            wrapper.eq(SmsCouponHistory::getCouponId, reqBo.getCouponId());
        }
        wrapper.orderByDesc(SmsCouponHistory::getCreateTime);
        IPage<SmsCouponHistory> pageList = smsCouponHistoryMapper.selectPage(page, wrapper);

        PageResponse<CouponHistoryVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    private CouponHistoryVo toVo(SmsCouponHistory entity) {
        CouponHistoryVo vo = new CouponHistoryVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
