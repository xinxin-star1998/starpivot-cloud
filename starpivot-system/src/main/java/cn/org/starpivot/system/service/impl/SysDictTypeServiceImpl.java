package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.bo.SysDictTypeVO;
import cn.org.starpivot.system.domain.dto.SysDictTypeDTO;
import cn.org.starpivot.system.domain.dto.SysDictTypeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysDictData;
import cn.org.starpivot.system.domain.entity.SysDictType;
import cn.org.starpivot.system.mapper.SysDictDataMapper;
import cn.org.starpivot.system.mapper.SysDictTypeMapper;
import cn.org.starpivot.system.service.SysDictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型服务实现类。
 * <p>实现 {@link SysDictTypeService}，含字典类型 CRUD 及下属数据存在性校验。</p>
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataMapper sysDictDataMapper;

    /**
     * 分页查询字典类型列表，支持按名称、类型、状态筛选。
     *
     * @param queryDTO 查询条件与分页参数
     * @return {@link SysDictTypeVO} 分页结果
     */
    @Override
    public PageResponse<SysDictTypeVO> selectDictTypePage(SysDictTypeQueryDTO queryDTO) {
        PageResponse<SysDictTypeVO> pageResponse = new PageResponse<>();
        Page<SysDictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        wrapper.like(StringUtils.hasText(queryDTO.getDictName()), SysDictType::getDictName, queryDTO.getDictName())
                .like(StringUtils.hasText(queryDTO.getDictType()), SysDictType::getDictType, queryDTO.getDictType())
                .eq(StringUtils.hasText(queryDTO.getStatus()), SysDictType::getStatus, queryDTO.getStatus())
                .orderByDesc(SysDictType::getCreateTime);

        IPage<SysDictType> dictTypePage = this.page(page, wrapper);

        // 转换为VO
        IPage<SysDictTypeVO> voPage = new Page<>(dictTypePage.getCurrent(), dictTypePage.getSize(), dictTypePage.getTotal());
        List<SysDictTypeVO> voList = dictTypePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        pageResponse.setTotal(dictTypePage.getTotal());
        pageResponse.setRows(voPage.getRecords());
        pageResponse.setPageNum(voPage.getCurrent());
        pageResponse.setPageSize(voPage.getSize());
        return pageResponse;
    }

    /**
     * 根据主键查询字典类型详情。
     *
     * @param dictId 字典类型主键
     * @return 字典类型视图对象
     * @throws cn.org.starpivot.common.exception.BizException 字典类型不存在时抛出
     */
    @Override
    public SysDictTypeVO selectDictTypeById(Long dictId) {
        SysDictType dictType = this.getById(dictId);
        AssertUtils.notNull(dictType, ErrorCode.DICT_TYPE_NOT_FOUND);
        return convertToVO(dictType);
    }

    /**
     * 新增字典类型，校验类型标识唯一性。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param dictTypeDTO 字典类型信息
     * @return 是否新增成功
     * @throws BizException 字典类型已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDictType(SysDictTypeDTO dictTypeDTO) {
        if (!checkDictTypeUnique(dictTypeDTO.getDictType(), null)) {
            throw new BizException(ErrorCode.DICT_TYPE_EXISTS, "字典类型已存在");
        }

        // 创建字典类型
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        dictType.setStatus(StringUtils.hasText(dictTypeDTO.getStatus()) ? dictTypeDTO.getStatus() : "0");

        String currentUser = SecurityContextUtils.getUsername();
        dictType.setCreateBy(currentUser);
        dictType.setCreateTime(LocalDateTime.now());

        return this.save(dictType);
    }

    /**
     * 修改字典类型；若类型标识变更，同步更新下属字典数据的类型字段。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param dictTypeDTO 字典类型信息
     * @return 是否修改成功
     * @throws BizException 字典类型不存在或类型标识已被使用时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(SysDictTypeDTO dictTypeDTO) {
        SysDictType dictType = this.getById(dictTypeDTO.getDictId());
        AssertUtils.notNull(dictType, ErrorCode.DICT_TYPE_NOT_FOUND);

        if (!checkDictTypeUnique(dictTypeDTO.getDictType(), dictTypeDTO.getDictId())) {
            throw new BizException(ErrorCode.DICT_TYPE_EXISTS, "字典类型已被使用");
        }

        if (!dictType.getDictType().equals(dictTypeDTO.getDictType())) {
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, dictType.getDictType());
            List<SysDictData> dictDataList = sysDictDataMapper.selectList(wrapper);
            for (SysDictData dictData : dictDataList) {
                dictData.setDictType(dictTypeDTO.getDictType());
                sysDictDataMapper.updateById(dictData);
            }
        }

        // 更新字典类型信息
        BeanUtils.copyProperties(dictTypeDTO, dictType, "dictId");
        String currentUser = SecurityContextUtils.getUsername();
        dictType.setUpdateBy(currentUser);
        dictType.setUpdateTime(LocalDateTime.now());

        return this.updateById(dictType);
    }

    /**
     * 批量删除字典类型，存在下属字典数据时不允许删除。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param dictIds 待删除的字典类型主键列表
     * @return 删除成功返回 {@code true}；列表为空返回 {@code false}
     * @throws BizException 字典类型下存在字典数据时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictTypeByIds(List<Long> dictIds) {
        if (dictIds == null || dictIds.isEmpty()) {
            return false;
        }

        for (Long dictId : dictIds) {
            SysDictType dictType = this.getById(dictId);
            if (dictType != null) {
                // 检查是否有字典数据
                LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysDictData::getDictType, dictType.getDictType());
                long count = sysDictDataMapper.selectCount(wrapper);
                if (count > 0) {
                    throw new BizException("字典类型[" + dictType.getDictName() + "]存在字典数据，不能删除");
                }

                this.removeById(dictId);
            }
        }
        return true;
    }

    /**
     * 校验字典类型标识是否唯一。
     *
     * @param dictType 字典类型标识
     * @param dictId   当前字典类型主键；新增时传 {@code null}，修改时用于排除自身
     * @return 类型标识唯一返回 {@code true}
     */
    @Override
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType);
        if (dictId != null) {
            wrapper.ne(SysDictType::getDictId, dictId);
        }
        return this.count(wrapper) == 0;
    }

    /**
     * 查询全部字典类型列表。
     *
     * @return {@link SysDictTypeVO} 列表
     */
    @Override
    public List<SysDictTypeVO> selectList() {
        return this.list().stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 将 {@link SysDictType} 实体转换为 {@link SysDictTypeVO}。
     *
     * @param dictType 字典类型实体
     * @return 字典类型视图对象
     */
    private SysDictTypeVO convertToVO(SysDictType dictType) {
        SysDictTypeVO vo = new SysDictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }
}