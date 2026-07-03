package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApplyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OmsOrderReturnApplyServiceImpl extends ServiceImpl<OmsOrderReturnApplyMapper, OmsOrderReturnApply>
        implements OmsOrderReturnApplyService {

    private final OmsOrderReturnFulfillmentService omsOrderReturnFulfillmentService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReturnVo> pageList(ReturnReqBo reqBo) {
        Page<OmsOrderReturnApply> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<OmsOrderReturnApply> applyPage = baseMapper.selectPageList(page, reqBo);

        List<ReturnVo> rows = applyPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());

        PageResponse<ReturnVo> response = new PageResponse<>();
        response.setTotal(applyPage.getTotal());
        response.setRows(rows);
        response.setPageNum(applyPage.getCurrent());
        response.setPageSize(applyPage.getSize());
        response.setPageCount(applyPage.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnVo getDetailById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "退货申请ID不能为空");
        }
        OmsOrderReturnApply apply = baseMapper.selectById(id);
        if (apply == null) {
            throw new BizException("退货申请不存在");
        }
        return toVo(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeReturn(Long id) {
        omsOrderReturnFulfillmentService.completeReturn(id);
    }

    private ReturnVo toVo(OmsOrderReturnApply apply) {
        ReturnVo vo = new ReturnVo();
        BeanUtils.copyProperties(apply, vo);
        return vo;
    }
}
