package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.mapper.UmsMemberLoginLogMapper;
import cn.org.starpivot.mall.ums.domain.bo.MemberLoginLogReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLoginLogVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberLoginLog;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberLoginLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UmsMemberLoginLogServiceImpl implements UmsMemberLoginLogService {

    private final UmsMemberLoginLogMapper loginLogMapper;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberLoginLogVo> pageList(MemberLoginLogReqBo reqBo) {
        Page<UmsMemberLoginLog> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsMemberLoginLog> wrapper = Wrappers.<UmsMemberLoginLog>lambdaQuery()
                .eq(reqBo.getMemberId() != null, UmsMemberLoginLog::getMemberId, reqBo.getMemberId())
                .eq(reqBo.getLoginType() != null, UmsMemberLoginLog::getLoginType, reqBo.getLoginType())
                .like(StringUtils.hasText(reqBo.getIp()), UmsMemberLoginLog::getIp, reqBo.getIp())
                .orderByDesc(UmsMemberLoginLog::getCreateTime)
                .orderByDesc(UmsMemberLoginLog::getId);
        IPage<UmsMemberLoginLog> pageList = loginLogMapper.selectPage(page, wrapper);

        List<Long> memberIds = pageList.getRecords().stream()
                .map(UmsMemberLoginLog::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UmsMember> memberMap = memberIds.isEmpty()
                ? Map.of()
                : umsMemberMapper.selectBatchIds(memberIds).stream()
                        .filter(m -> m.getId() != null)
                        .collect(Collectors.toMap(UmsMember::getId, m -> m, (a, b) -> a));

        PageResponse<MemberLoginLogVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        response.setRows(pageList.getRecords().stream()
                .map(log -> toVo(log, memberMap.get(log.getMemberId())))
                .toList());
        response.setPageNum(pageList.getCurrent());
        response.setPageSize(pageList.getSize());
        response.setPageCount(pageList.getPages());
        return response;
    }

    private MemberLoginLogVo toVo(UmsMemberLoginLog log, UmsMember member) {
        MemberLoginLogVo vo = new MemberLoginLogVo();
        BeanUtils.copyProperties(log, vo);
        if (member != null) {
            vo.setMemberUsername(member.getUsername());
            vo.setMemberNickname(member.getNickname());
        }
        vo.setLoginTypeLabel(resolveLoginTypeLabel(log.getLoginType()));
        return vo;
    }

    private String resolveLoginTypeLabel(Integer loginType) {
        if (loginType == null) {
            return "-";
        }
        try {
            return PortalAuthType.fromCode(loginType).getLabel();
        } catch (IllegalArgumentException ex) {
            return String.valueOf(loginType);
        }
    }
}
