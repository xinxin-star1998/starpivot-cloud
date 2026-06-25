package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.oms.domain.bo.ReturnAuditBo;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApplyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退货申请服务实现类。
 * <p>
 * 实现 {@link OmsOrderReturnApplyService}，处理退货申请相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 * </ul>
 *
 * @see OmsOrderReturnApplyService
 */

@Service
public class OmsOrderReturnApplyServiceImpl extends ServiceImpl<OmsOrderReturnApplyMapper, OmsOrderReturnApply>
        implements OmsOrderReturnApplyService {

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
    public void audit(ReturnAuditBo bo) {
        OmsOrderReturnApply apply = baseMapper.selectById(bo.getId());
        if (apply == null) {
            throw new BizException("退货申请不存在");
        }
        apply.setStatus(bo.getStatus());
        apply.setHandleNote(bo.getHandleNote());
        String handleMan = StringUtils.hasText(bo.getHandleMan())
                ? bo.getHandleMan()
                : SecurityContextUtils.getUsername();
        apply.setHandleMan(handleMan);
        apply.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(apply);
    }

    private ReturnVo toVo(OmsOrderReturnApply apply) {
        ReturnVo vo = new ReturnVo();
        BeanUtils.copyProperties(apply, vo);
        return vo;
    }
}
