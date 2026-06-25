package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.pms.domain.dto.GroupAttrRelationSaveDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrGroupExcel;
import cn.org.starpivot.mall.pms.domain.vo.GroupAttrRelationVO;
import cn.org.starpivot.mall.pms.domain.vo.PmsAttrGroupVO;
import cn.org.starpivot.mall.pms.entity.PmsAttr;
import cn.org.starpivot.mall.pms.entity.PmsAttrAttrgroupRelation;
import cn.org.starpivot.mall.pms.entity.PmsAttrGroup;
import cn.org.starpivot.mall.pms.mapper.PmsAttrAttrgroupRelationMapper;
import cn.org.starpivot.mall.pms.mapper.PmsAttrGroupMapper;
import cn.org.starpivot.mall.pms.mapper.PmsAttrMapper;
import cn.org.starpivot.mall.pms.service.PmsAttrGroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性分组服务实现类。
 * <p>
 * 实现 {@link PmsAttrGroupService}，处理属性分组相关业务。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PmsAttrGroupService
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PmsAttrGroupServiceImpl extends ServiceImpl<PmsAttrGroupMapper, PmsAttrGroup> implements PmsAttrGroupService
{

    /** 基本属性（规格参数） */
    private static final int ATTR_TYPE_BASE = 1;

    private final PmsAttrMapper pmsAttrMapper;
    private final PmsAttrAttrgroupRelationMapper attrAttrgroupRelationMapper;

    /**
     * 分页查询属性分组列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<PmsAttrGroupVO> selectPmsAttrGroupPage(PmsAttrGroupQueryDTO queryDTO)
    {
        PageResponse<PmsAttrGroupVO> pageResponse = new PageResponse<>();
        Page<PmsAttrGroup> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PmsAttrGroup> pmsAttrGroupPage = baseMapper.selectPageList(page, queryDTO);
        
        // 转换为VO
        java.util.List<PmsAttrGroupVO> voList = pmsAttrGroupPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
        
        pageResponse.setTotal(pmsAttrGroupPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(pmsAttrGroupPage.getCurrent());
        pageResponse.setPageSize(pmsAttrGroupPage.getSize());
        pageResponse.setPageCount(pmsAttrGroupPage.getPages());
        return pageResponse;
    }

    /**
     * 根据主键查询属性分组详细信息
     * 
     * @param attrGroupId 属性分组主键
     * @return 属性分组信息
     */
    @Override
    public PmsAttrGroupVO selectPmsAttrGroupByAttrGroupId(Long attrGroupId)
    {
        PmsAttrGroup pmsAttrGroup = this.getById(attrGroupId);
        if (pmsAttrGroup == null) {
            throw new BizException("属性分组不存在");
        }
        return convertToVO(pmsAttrGroup);
    }

    /**
     * 新增属性分组
     * 
     * @param pmsAttrGroupDTO 属性分组信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertPmsAttrGroup(PmsAttrGroupDTO pmsAttrGroupDTO)
    {
        PmsAttrGroup pmsAttrGroup = new PmsAttrGroup();
        BeanUtils.copyProperties(pmsAttrGroupDTO, pmsAttrGroup);
        return this.save(pmsAttrGroup);
    }

    /**
     * 修改属性分组
     * 
     * @param pmsAttrGroupDTO 属性分组信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updatePmsAttrGroup(PmsAttrGroupDTO pmsAttrGroupDTO)
    {
        PmsAttrGroup pmsAttrGroup = this.getById(pmsAttrGroupDTO.getAttrGroupId());
        if (pmsAttrGroup == null) {
            throw new BizException("属性分组不存在");
        }
        
        BeanUtils.copyProperties(pmsAttrGroupDTO, pmsAttrGroup, "attrGroupId");
        return this.updateById(pmsAttrGroup);
    }

    /**
     * 批量删除属性分组
     * 
     * @param attrGroupIds 需要删除的属性分组主键数组
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePmsAttrGroupByAttrGroupIds(Long[] attrGroupIds)
    {
        if (attrGroupIds.length > 0) {
            attrAttrgroupRelationMapper.delete(
                    Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                            .in(PmsAttrAttrgroupRelation::getAttrGroupId, java.util.Arrays.asList(attrGroupIds)));
        }
        return this.removeByIds(java.util.Arrays.asList(attrGroupIds));
    }

    @Override
    public List<GroupAttrRelationVO> listGroupAttrRelations(Long attrGroupId) {
        PmsAttrGroup group = requireGroup(attrGroupId);
        Long catelogId = group.getCatelogId();
        if (catelogId == null) {
            return Collections.emptyList();
        }
        List<PmsAttr> attrs =
                pmsAttrMapper.selectList(
                        Wrappers.<PmsAttr>lambdaQuery()
                                .eq(PmsAttr::getCatelogId, catelogId)
                                .eq(PmsAttr::getAttrType, ATTR_TYPE_BASE)
                                .orderByAsc(PmsAttr::getAttrId));
        if (attrs.isEmpty()) {
            return Collections.emptyList();
        }
        List<PmsAttrAttrgroupRelation> relations =
                attrAttrgroupRelationMapper.selectList(
                        Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                                .eq(PmsAttrAttrgroupRelation::getAttrGroupId, attrGroupId));
        Map<Long, Integer> sortByAttrId =
                relations.stream()
                        .filter(r -> r.getAttrId() != null)
                        .collect(
                                Collectors.toMap(
                                        PmsAttrAttrgroupRelation::getAttrId,
                                        r -> r.getAttrSort() != null ? r.getAttrSort() : 0,
                                        (a, b) -> a));
        List<GroupAttrRelationVO> result = new ArrayList<>(attrs.size());
        for (PmsAttr attr : attrs) {
            GroupAttrRelationVO vo = new GroupAttrRelationVO();
            vo.setAttrId(attr.getAttrId());
            vo.setAttrName(attr.getAttrName());
            vo.setIcon(attr.getIcon());
            vo.setValueSelect(attr.getValueSelect());
            boolean linked = sortByAttrId.containsKey(attr.getAttrId());
            vo.setLinked(linked);
            vo.setAttrSort(linked ? sortByAttrId.get(attr.getAttrId()) : null);
            result.add(vo);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveGroupAttrRelations(Long attrGroupId, GroupAttrRelationSaveDTO saveDTO) {
        PmsAttrGroup group = requireGroup(attrGroupId);
        Long catelogId = group.getCatelogId();
        if (catelogId == null) {
            throw new BizException("分组未设置所属分类，无法关联属性");
        }
        attrAttrgroupRelationMapper.delete(
                Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                        .eq(PmsAttrAttrgroupRelation::getAttrGroupId, attrGroupId));
        List<GroupAttrRelationSaveDTO.GroupAttrBindItemDTO> items =
                saveDTO.getItems() != null ? saveDTO.getItems() : Collections.emptyList();
        int defaultSort = 0;
        for (GroupAttrRelationSaveDTO.GroupAttrBindItemDTO item : items) {
            if (item == null || item.getAttrId() == null) {
                continue;
            }
            Long attrId = item.getAttrId();
            PmsAttr attr = pmsAttrMapper.selectById(attrId);
            if (attr == null) {
                throw new BizException("属性不存在: " + attrId);
            }
            if (!Objects.equals(catelogId, attr.getCatelogId())) {
                throw new BizException("属性「" + attr.getAttrName() + "」与分组不属于同一分类");
            }
            if (!Integer.valueOf(ATTR_TYPE_BASE).equals(attr.getAttrType())) {
                throw new BizException("仅可关联基本属性（规格参数）");
            }
            attrAttrgroupRelationMapper.delete(
                    Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                            .eq(PmsAttrAttrgroupRelation::getAttrId, attrId));
            PmsAttrAttrgroupRelation relation = new PmsAttrAttrgroupRelation();
            relation.setAttrId(attrId);
            relation.setAttrGroupId(attrGroupId);
            relation.setAttrSort(
                    item.getAttrSort() != null ? item.getAttrSort() : defaultSort++);
            attrAttrgroupRelationMapper.insert(relation);
        }
        return true;
    }

    private PmsAttrGroup requireGroup(Long attrGroupId) {
        PmsAttrGroup group = this.getById(attrGroupId);
        if (group == null) {
            throw new BizException("属性分组不存在");
        }
        return group;
    }

    /**
     * 转换为VO
     * 
     * @param pmsAttrGroup 实体对象
     * @return VO对象
     */
    private PmsAttrGroupVO convertToVO(PmsAttrGroup pmsAttrGroup)
    {
        PmsAttrGroupVO vo = new PmsAttrGroupVO();
        BeanUtils.copyProperties(pmsAttrGroup, vo);
        return vo;
    }

    @Override
    public List<PmsAttrGroupExcel> listForExport(PmsAttrGroupQueryDTO queryDTO) {
        PmsAttrGroupQueryDTO q = queryDTO != null ? queryDTO : new PmsAttrGroupQueryDTO();
        return baseMapper.selectListByQuery(q).stream().map(this::toExcelRow).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int importFromExcel(List<PmsAttrGroupExcel> rows, boolean updateSupport) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (PmsAttrGroupExcel row : rows) {
            if (row == null || isBlankRow(row)) {
                continue;
            }
            if (row.getAttrGroupName() == null || row.getAttrGroupName().isBlank()) {
                throw new BizException("导入失败：组名不能为空");
            }
            if (row.getCatelogId() == null) {
                throw new BizException("导入失败：所属分类ID不能为空（组名：" + row.getAttrGroupName() + "）");
            }
            PmsAttrGroupDTO dto = new PmsAttrGroupDTO();
            dto.setAttrGroupId(row.getAttrGroupId());
            dto.setAttrGroupName(row.getAttrGroupName().trim());
            dto.setSort(row.getSort());
            dto.setDescript(row.getDescript());
            dto.setIcon(row.getIcon());
            dto.setCatelogId(row.getCatelogId());
            if (row.getAttrGroupId() != null && updateSupport) {
                PmsAttrGroup existing = this.getById(row.getAttrGroupId());
                if (existing != null) {
                    if (!updatePmsAttrGroup(dto)) {
                        throw new BizException("更新属性分组失败，ID=" + row.getAttrGroupId());
                    }
                    count++;
                    continue;
                }
            }
            if (!insertPmsAttrGroup(dto)) {
                throw new BizException("新增属性分组失败：" + row.getAttrGroupName());
            }
            count++;
        }
        return count;
    }

    private PmsAttrGroupExcel toExcelRow(PmsAttrGroup entity) {
        PmsAttrGroupExcel row = new PmsAttrGroupExcel();
        BeanUtils.copyProperties(entity, row);
        return row;
    }

    private boolean isBlankRow(PmsAttrGroupExcel row) {
        return (row.getAttrGroupName() == null || row.getAttrGroupName().isBlank())
                && row.getCatelogId() == null
                && row.getAttrGroupId() == null;
    }
}
