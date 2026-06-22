package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务实现类。
 * <p>
 * 实现 {@link SysLogininforService}，异步保存登录日志并提供分页查询与统计。
 * </p>
 */
@Slf4j
@Service
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor>
        implements SysLogininforService {

    /**
     * 异步保存登录日志，失败仅记录错误不阻断主流程。
     * <p>使用 {@code @Async("taskExecutor")} 在线程池中异步执行，不阻塞登录主流程。</p>
     *
     * @param logininfor 登录日志实体
     */
    @Override
    @Async("taskExecutor")
    public void saveLogininfor(SysLogininfor logininfor) {
        try {
            this.save(logininfor);
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 分页查询登录日志，支持按用户名、IP、状态及登录时间范围筛选。
     *
     * @param logininforReqBo 查询条件与分页参数
     * @return 登录日志 {@link LogininforVO} 分页结果
     */
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

    /**
     * 统计指定时间范围内各月份的登录次数，用于控制台趋势图表。
     *
     * @param start 起始时间（含）
     * @param end   结束时间（含）
     * @return 月份与登录次数的映射列表
     */
    @Override
    public List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end) {
        return getBaseMapper().countByMonthRange(start, end);
    }

    /**
     * 统计指定用户自某时间点以来的登录次数。
     *
     * @param userNames 用户名列表
     * @param start     统计起始时间（含）
     * @return 用户名与登录次数的映射列表
     */
    @Override
    public List<Map<String, Object>> countByUserNames(List<String> userNames, LocalDateTime start) {
        return getBaseMapper().countByUserNames(userNames, start);
    }

    /**
     * 将 {@link SysLogininfor} 实体转换为 {@link LogininforVO}。
     *
     * @param logininfor 登录日志实体
     * @return 登录日志视图对象
     */
    private LogininforVO convertToVO(SysLogininfor logininfor) {
        LogininforVO vo = new LogininforVO();
        BeanUtils.copyProperties(logininfor, vo);
        return vo;
    }
}
