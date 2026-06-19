package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.system.domain.bo.LogininforReqBo;
import cn.org.starpivot.system.domain.bo.LogininforVO;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import cn.org.starpivot.system.mapper.SysLogininforMapper;
import cn.org.starpivot.system.service.SysLogininforService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor>
        implements SysLogininforService {

    @Override
    @Async("taskExecutor")
    public void saveLogininfor(SysLogininfor logininfor) {
        try {
            this.save(logininfor);
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public PageResponse<LogininforVO> pageList(LogininforReqBo logininforReqBo) {
        LambdaQueryWrapper<SysLogininfor> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(logininforReqBo.getUserName())) {
            queryWrapper.like(SysLogininfor::getUserName, logininforReqBo.getUserName());
        }
        if (StringUtils.hasText(logininforReqBo.getIpaddr())) {
            queryWrapper.like(SysLogininfor::getIpaddr, logininforReqBo.getIpaddr());
        }
        if (StringUtils.hasText(logininforReqBo.getStatus())) {
            queryWrapper.eq(SysLogininfor::getStatus, logininforReqBo.getStatus());
        }
        if (logininforReqBo.getStartTime() != null) {
            queryWrapper.ge(SysLogininfor::getLoginTime, logininforReqBo.getStartTime());
        }
        if (logininforReqBo.getEndTime() != null) {
            queryWrapper.le(SysLogininfor::getLoginTime, logininforReqBo.getEndTime());
        }
        queryWrapper.orderByDesc(SysLogininfor::getLoginTime);

        Page<SysLogininfor> page = new Page<>(logininforReqBo.getPageNum(), logininforReqBo.getPageSize());
        IPage<SysLogininfor> pageList = this.page(page, queryWrapper);

        List<LogininforVO> voList = pageList.getRecords().stream().map(this::convertToVO).toList();

        PageResponse<LogininforVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    private LogininforVO convertToVO(SysLogininfor logininfor) {
        LogininforVO vo = new LogininforVO();
        BeanUtils.copyProperties(logininfor, vo);
        return vo;
    }
}
