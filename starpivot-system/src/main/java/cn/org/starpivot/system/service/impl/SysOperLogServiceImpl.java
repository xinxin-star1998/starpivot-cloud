package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.OperLogReqBo;
import cn.org.starpivot.system.domain.bo.OperLogVO;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.mapper.SysOperLogMapper;
import cn.org.starpivot.system.service.SysOperLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public PageResponse<OperLogVO> pageList(OperLogReqBo operLogReqBo) {
        LambdaQueryWrapper<SysOperLog> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(operLogReqBo.getTitle())) {
            queryWrapper.like(SysOperLog::getTitle, operLogReqBo.getTitle());
        }
        if (operLogReqBo.getBusinessType() != null) {
            queryWrapper.eq(SysOperLog::getBusinessType, operLogReqBo.getBusinessType());
        }
        if (StringUtils.hasText(operLogReqBo.getOperName())) {
            queryWrapper.like(SysOperLog::getOperName, operLogReqBo.getOperName());
        }
        if (operLogReqBo.getStatus() != null) {
            queryWrapper.eq(SysOperLog::getStatus, operLogReqBo.getStatus());
        }
        if (operLogReqBo.getStartTime() != null) {
            queryWrapper.ge(SysOperLog::getOperTime, operLogReqBo.getStartTime());
        }
        if (operLogReqBo.getEndTime() != null) {
            queryWrapper.le(SysOperLog::getOperTime, operLogReqBo.getEndTime());
        }
        queryWrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = new Page<>(operLogReqBo.getPageNum(), operLogReqBo.getPageSize());
        IPage<SysOperLog> pageList = this.page(page, queryWrapper);

        List<OperLogVO> voList = pageList.getRecords().stream().map(this::convertToVO).toList();

        PageResponse<OperLogVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    private OperLogVO convertToVO(SysOperLog operLog) {
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(operLog, vo);
        return vo;
    }
}
