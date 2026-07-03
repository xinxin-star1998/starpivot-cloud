package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillSessionVo;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSession;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSessionMapper;
import cn.org.starpivot.mall.sms.service.SmsSeckillSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀场次服务实现类。
 * <p>
 * 实现 {@link SmsSeckillSessionService}，处理秒杀场次相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsSeckillSessionService
 */

@Service
@RequiredArgsConstructor
public class SmsSeckillSessionServiceImpl implements SmsSeckillSessionService {

    private final SmsSeckillSessionMapper smsSeckillSessionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeckillSessionVo> pageList(SeckillSessionReqBo reqBo) {
        Page<SmsSeckillSession> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsSeckillSession> wrapper = buildQueryWrapper(reqBo);
        IPage<SmsSeckillSession> pageList = smsSeckillSessionMapper.selectPage(page, wrapper);

        PageResponse<SeckillSessionVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeckillSessionVo> listAll() {
        return smsSeckillSessionMapper.selectList(
                        Wrappers.<SmsSeckillSession>lambdaQuery()
                                .eq(SmsSeckillSession::getStatus, 1)
                                .orderByAsc(SmsSeckillSession::getStartTime)
                                .orderByAsc(SmsSeckillSession::getId))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SeckillSessionVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "场次ID不能为空");
        }
        SmsSeckillSession entity = smsSeckillSessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("秒杀场次不存在");
        }
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SeckillSessionSaveBo bo) {
        SmsSeckillSession entity = new SmsSeckillSession();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        smsSeckillSessionMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SeckillSessionSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改场次时 id 不能为空");
        }
        SmsSeckillSession existing = smsSeckillSessionMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("秒杀场次不存在");
        }
        SmsSeckillSession entity = new SmsSeckillSession();
        BeanUtils.copyProperties(bo, entity);
        smsSeckillSessionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsSeckillSessionMapper.delete(
                Wrappers.<SmsSeckillSession>lambdaQuery().in(SmsSeckillSession::getId, ids));
    }

    private LambdaQueryWrapper<SmsSeckillSession> buildQueryWrapper(SeckillSessionReqBo reqBo) {
        LambdaQueryWrapper<SmsSeckillSession> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getName())) {
            wrapper.like(SmsSeckillSession::getName, reqBo.getName());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsSeckillSession::getStatus, reqBo.getStatus());
        }
        wrapper.orderByAsc(SmsSeckillSession::getStartTime).orderByDesc(SmsSeckillSession::getId);
        return wrapper;
    }

    private SeckillSessionVo toVo(SmsSeckillSession entity) {
        SeckillSessionVo vo = new SeckillSessionVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
