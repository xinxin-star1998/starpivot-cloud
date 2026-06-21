package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.bo.SysNoticeVO;
import cn.org.starpivot.system.domain.dto.SysNoticeDTO;
import cn.org.starpivot.system.domain.dto.SysNoticeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysNotice;
import cn.org.starpivot.system.mapper.SysNoticeMapper;
import cn.org.starpivot.system.service.ISysNoticeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知公告Service业务层实现
 * 
 * @author admin
 * @date 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService
{

    /**
     * 分页查询通知公告列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<SysNoticeVO> selectSysNoticePage(SysNoticeQueryDTO queryDTO)
    {
        PageResponse<SysNoticeVO> pageResponse = new PageResponse<>();
        Page<SysNotice> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysNotice> sysNoticePage = baseMapper.selectPageList(page, queryDTO);
        
        // 转换为VO
        java.util.List<SysNoticeVO> voList = sysNoticePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
        
        pageResponse.setTotal(sysNoticePage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(sysNoticePage.getCurrent());
        pageResponse.setPageSize(sysNoticePage.getSize());
        pageResponse.setPageCount(sysNoticePage.getPages());
        return pageResponse;
    }

    /**
     * 根据主键查询通知公告详细信息
     * 
     * @param noticeId 通知公告主键
     * @return 通知公告信息
     */
    @Override
    public SysNoticeVO selectSysNoticeByNoticeId(Integer noticeId)
    {
        SysNotice sysNotice = this.getById(noticeId);
        AssertUtils.notNull(sysNotice, ErrorCode.NOTICE_NOT_FOUND);
        return convertToVO(sysNotice);
    }

    /**
     * 新增通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertSysNotice(SysNoticeDTO sysNoticeDTO)
    {
        SysNotice sysNotice = new SysNotice();
        BeanUtils.copyProperties(sysNoticeDTO, sysNotice);
        sysNotice.setCreateBy(getCurrentUsername());
        sysNotice.setCreateTime(java.time.LocalDateTime.now());
        return this.save(sysNotice);
    }

    /**
     * 修改通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateSysNotice(SysNoticeDTO sysNoticeDTO)
    {
        SysNotice sysNotice = this.getById(sysNoticeDTO.getNoticeId());
        AssertUtils.notNull(sysNotice, ErrorCode.NOTICE_NOT_FOUND);
        BeanUtils.copyProperties(sysNoticeDTO, sysNotice, "noticeId");
        sysNotice.setUpdateBy(getCurrentUsername());
        sysNotice.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(sysNotice);
    }

    /**
     * 批量删除通知公告
     * 
     * @param noticeIds 需要删除的通知公告主键数组
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteSysNoticeByNoticeIds(List<Long> noticeIds)
    {
        return this.removeByIds(noticeIds);
    }

    /**
     * 转换为VO
     * 
     * @param sysNotice 实体对象
     * @return VO对象
     */
    private SysNoticeVO convertToVO(SysNotice sysNotice)
    {
        SysNoticeVO vo = new SysNoticeVO();
        BeanUtils.copyProperties(sysNotice, vo);
        return vo;
    }
    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户名
     * @throws cn.org.starpivot.common.exception.BizException 当用户未登录时抛出异常
     */
    private String getCurrentUsername() {
        String username = SecurityContextUtils.getUsername();
        if (username == null) {
            log.warn("获取当前用户名失败：用户未登录或认证信息不存在");
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户未登录或认证信息不存在");
        }
        return username;
    }
}
