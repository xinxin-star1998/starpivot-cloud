package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.ums.domain.bo.MemberLevelSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLevelVo;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberLevelService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 会员等级服务实现类。
 * <p>
 * 实现 {@link UmsMemberLevelService}，处理会员等级相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see UmsMemberLevelService
 */

@Service
@RequiredArgsConstructor
public class UmsMemberLevelServiceImpl implements UmsMemberLevelService {

    private final UmsMemberLevelMapper umsMemberLevelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberLevelVo> listAll() {
        return umsMemberLevelMapper
                .selectList(Wrappers.<UmsMemberLevel>lambdaQuery().orderByAsc(UmsMemberLevel::getGrowthPoint))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberLevelVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "等级ID不能为空");
        }
        UmsMemberLevel entity = umsMemberLevelMapper.selectById(id);
        if (entity == null) {
            throw new BizException("会员等级不存在");
        }
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MemberLevelSaveBo bo) {
        UmsMemberLevel entity = new UmsMemberLevel();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        umsMemberLevelMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberLevelSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改等级时 id 不能为空");
        }
        UmsMemberLevel existing = umsMemberLevelMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("会员等级不存在");
        }
        UmsMemberLevel entity = new UmsMemberLevel();
        BeanUtils.copyProperties(bo, entity);
        umsMemberLevelMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        umsMemberLevelMapper.delete(Wrappers.<UmsMemberLevel>lambdaQuery().in(UmsMemberLevel::getId, ids));
    }

    private MemberLevelVo toVo(UmsMemberLevel entity) {
        MemberLevelVo vo = new MemberLevelVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
