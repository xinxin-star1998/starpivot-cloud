package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberStatisticsInfo;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberStatisticsInfoMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberStatisticsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员统计服务实现类。
 * <p>
 * 实现 {@link UmsMemberStatisticsService}，处理会员统计相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see UmsMemberStatisticsService
 */

@Service
@RequiredArgsConstructor
public class UmsMemberStatisticsServiceImpl implements UmsMemberStatisticsService {

    private final UmsMemberStatisticsInfoMapper statisticsInfoMapper;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberStatisticsVo> pageList(MemberStatisticsReqBo reqBo) {
        Page<MemberStatisticsVo> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<MemberStatisticsVo> pageList = statisticsInfoMapper.selectPageList(page, reqBo);

        PageResponse<MemberStatisticsVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords());
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberStatisticsVo getByMemberId(Long memberId) {
        requireMember(memberId);
        MemberStatisticsReqBo reqBo = new MemberStatisticsReqBo();
        reqBo.setMemberId(memberId);
        reqBo.setPageNum(1);
        reqBo.setPageSize(1);
        PageResponse<MemberStatisticsVo> pageResponse = pageList(reqBo);
        if (pageResponse.getRows().isEmpty()) {
            throw new BizException("会员不存在");
        }
        return pageResponse.getRows().get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refresh(Long memberId) {
        requireMember(memberId);
        UmsMemberStatisticsInfo aggregated = statisticsInfoMapper.aggregateByMemberId(memberId);
        UmsMemberStatisticsInfo existing = statisticsInfoMapper.selectOne(
                Wrappers.<UmsMemberStatisticsInfo>lambdaQuery()
                        .eq(UmsMemberStatisticsInfo::getMemberId, memberId));
        if (existing == null) {
            statisticsInfoMapper.insert(aggregated);
        } else {
            aggregated.setId(existing.getId());
            statisticsInfoMapper.updateById(aggregated);
        }
    }

    private void requireMember(Long memberId) {
        if (memberId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "会员ID不能为空");
        }
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }
    }
}
