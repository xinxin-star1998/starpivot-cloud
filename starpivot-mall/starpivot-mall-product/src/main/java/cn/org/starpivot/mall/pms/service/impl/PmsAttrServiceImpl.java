package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrExcel;
import cn.org.starpivot.mall.pms.domain.vo.PmsAttrVO;
import cn.org.starpivot.mall.pms.entity.PmsAttr;
import cn.org.starpivot.mall.pms.entity.PmsAttrAttrgroupRelation;
import cn.org.starpivot.mall.pms.entity.PmsAttrGroup;
import cn.org.starpivot.mall.pms.mapper.PmsAttrAttrgroupRelationMapper;
import cn.org.starpivot.mall.pms.mapper.PmsAttrGroupMapper;
import cn.org.starpivot.mall.pms.mapper.PmsAttrMapper;
import cn.org.starpivot.mall.pms.service.PmsAttrService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品属性服务实现类。
 * <p>
 * 实现 {@link PmsAttrService}，处理商品属性相关业务。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PmsAttrService
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PmsAttrServiceImpl extends ServiceImpl<PmsAttrMapper, PmsAttr> implements PmsAttrService {

    /** 属性 ↔ 属性分组 关联表 */
    private final PmsAttrAttrgroupRelationMapper attrAttrgroupRelationMapper;

    /** 校验分组是否存在、分类是否一致 */
    private final PmsAttrGroupMapper pmsAttrGroupMapper;

    @Override
    public PageResponse<PmsAttrVO> selectPmsAttrPage(PmsAttrQueryDTO queryDTO) {
        assertAttrTypePermission(queryDTO.getAttrType(), "query");
        PageResponse<PmsAttrVO> pageResponse = new PageResponse<>();
        Page<PmsAttr> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PmsAttr> pmsAttrPage = baseMapper.selectPageList(page, queryDTO);

        // 每条记录从关联表补全 attrGroupId、attrSort
        List<PmsAttrVO> voList =
                pmsAttrPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        pageResponse.setTotal(pmsAttrPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(pmsAttrPage.getCurrent());
        pageResponse.setPageSize(pmsAttrPage.getSize());
        pageResponse.setPageCount(pmsAttrPage.getPages());
        return pageResponse;
    }

    @Override
    public PmsAttrVO selectPmsAttrByAttrId(Long attrId) {
        PmsAttr pmsAttr = this.getById(attrId);
        if (pmsAttr == null) {
            throw new BizException("商品属性不存在");
        }
        assertAttrTypePermission(pmsAttr.getAttrType(), "query");
        return convertToVO(pmsAttr);
    }

    /**
     * 先写 pms_attr，再写关联表（全量覆盖该 attr 的关联行）。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertPmsAttr(PmsAttrDTO pmsAttrDTO) {
        assertAttrTypePermission(pmsAttrDTO.getAttrType(), "add");
        PmsAttr pmsAttr = new PmsAttr();
        BeanUtils.copyProperties(pmsAttrDTO, pmsAttr);
        applyValueSelectPolicy(pmsAttr);
        if (!this.save(pmsAttr)) {
            return false;
        }
        saveAttrGroupRelation(
                pmsAttr.getAttrId(),
                pmsAttrDTO.getAttrGroupId(),
                pmsAttrDTO.getAttrSort(),
                pmsAttr.getCatelogId());
        return true;
    }

    /**
     * 更新 pms_attr 后，按 DTO 全量替换关联（未传 attrGroupId 则仅删除原关联）。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updatePmsAttr(PmsAttrDTO pmsAttrDTO) {
        PmsAttr pmsAttr = this.getById(pmsAttrDTO.getAttrId());
        if (pmsAttr == null) {
            throw new BizException("商品属性不存在");
        }
        assertAttrTypePermission(pmsAttr.getAttrType(), "edit");
        if (pmsAttrDTO.getAttrType() != null && !pmsAttrDTO.getAttrType().equals(pmsAttr.getAttrType())) {
            throw new BizException("不允许修改属性类型");
        }

        BeanUtils.copyProperties(pmsAttrDTO, pmsAttr, "attrId");
        applyValueSelectPolicy(pmsAttr);
        if (!this.updateById(pmsAttr)) {
            return false;
        }
        Long catelogId = pmsAttrDTO.getCatelogId() != null ? pmsAttrDTO.getCatelogId() : pmsAttr.getCatelogId();
        saveAttrGroupRelation(
                pmsAttr.getAttrId(),
                pmsAttrDTO.getAttrGroupId(),
                pmsAttrDTO.getAttrSort(),
                catelogId);
        return true;
    }

    /**
     * 先删关联表，再删属性主表。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePmsAttrByAttrIds(Long[] attrIds) {
        for (Long attrId : attrIds) {
            PmsAttr row = this.getById(attrId);
            if (row != null) {
                assertAttrTypePermission(row.getAttrType(), "delete");
            }
        }
        if (attrIds.length > 0) {
            attrAttrgroupRelationMapper.delete(
                    Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                            .in(PmsAttrAttrgroupRelation::getAttrId, Arrays.asList(attrIds)));
        }
        return this.removeByIds(Arrays.asList(attrIds));
    }

    /** 空串不落库；单选/多选均可配置 value_select 候选值。 */
    private void applyValueSelectPolicy(PmsAttr entity) {
        String vs = entity.getValueSelect();
        if (vs == null || vs.isBlank()) {
            entity.setValueSelect(null);
        } else {
            entity.setValueSelect(vs.trim());
        }
    }

    /**
     * 保存属性与分组的关联：先删该 attr 下旧关联，再按需插入一条。
     *
     * @param attrId      属性 id（已落库）
     * @param attrGroupId 分组 id，null 表示解除关联
     * @param attrSort    组内排序，null 按 0
     * @param catelogId   属性所属三级分类，用于校验分组与分类一致
     */
    private void saveAttrGroupRelation(Long attrId, Long attrGroupId, Integer attrSort, Long catelogId) {
        if (attrId == null) {
            return;
        }
        attrAttrgroupRelationMapper.delete(
                Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                        .eq(PmsAttrAttrgroupRelation::getAttrId, attrId));
        if (attrGroupId == null) {
            return;
        }
        PmsAttrGroup group = pmsAttrGroupMapper.selectById(attrGroupId);
        if (group == null) {
            throw new BizException("属性分组不存在");
        }
        if (catelogId != null
                && group.getCatelogId() != null
                && !Objects.equals(group.getCatelogId(), catelogId)) {
            throw new BizException("属性分组与所属分类不一致");
        }
        PmsAttrAttrgroupRelation relation = new PmsAttrAttrgroupRelation();
        relation.setAttrId(attrId);
        relation.setAttrGroupId(attrGroupId);
        relation.setAttrSort(attrSort != null ? attrSort : 0);
        attrAttrgroupRelationMapper.insert(relation);
    }

    /**
     * 从 pms_attr_attrgroup_relation 回填 VO 中的分组信息。
     */
    private void fillAttrGroupRelation(PmsAttrVO vo) {
        if (vo.getAttrId() == null) {
            return;
        }
        PmsAttrAttrgroupRelation relation =
                attrAttrgroupRelationMapper.selectOne(
                        Wrappers.<PmsAttrAttrgroupRelation>lambdaQuery()
                                .eq(PmsAttrAttrgroupRelation::getAttrId, vo.getAttrId())
                                .last("LIMIT 1"));
        if (relation != null) {
            vo.setAttrGroupId(relation.getAttrGroupId());
            vo.setAttrSort(relation.getAttrSort());
        }
    }

    /**
     * 按属性类型校验当前用户是否具备对应权限标识。
     *
     * @param attrType 0-销售属性 mall:sale:*，1-基本属性 mall:base:*
     * @param action   query / add / edit / delete
     */
    private void assertAttrTypePermission(Integer attrType, String action) {
        if (attrType == null) {
            return;
        }
        String required = (Integer.valueOf(1).equals(attrType) ? "mall:base:" : "mall:sale:") + action;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(ErrorCode.FORBIDDEN, "未登录或无访问权限");
        }
        List<String> authorities =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();
        if (authorities.contains(required)) {
            return;
        }
        // 发布/编辑商品时只读拉取分类下属性（仅 query）
        if ("query".equals(action)
                && authorities.stream()
                        .anyMatch(
                                a ->
                                        "mall:product:query".equals(a)
                                                || "mall:product:add".equals(a)
                                                || "mall:product:edit".equals(a))) {
            return;
        }
        throw new BizException(ErrorCode.FORBIDDEN, "无权限操作该类型属性");
    }

    /** 实体转 VO，并合并关联表中的分组字段。 */
    private PmsAttrVO convertToVO(PmsAttr pmsAttr) {
        PmsAttrVO vo = new PmsAttrVO();
        BeanUtils.copyProperties(pmsAttr, vo);
        fillAttrGroupRelation(vo);
        return vo;
    }

    @Override
    public List<PmsAttrExcel> listForExport(PmsAttrQueryDTO queryDTO) {
        if (queryDTO == null || queryDTO.getAttrType() == null) {
            throw new BizException("导出必须指定属性类型");
        }
        assertAttrTypePermission(queryDTO.getAttrType(), "export");
        PmsAttrQueryDTO q = queryDTO;
        return baseMapper.selectListByQuery(q).stream()
                .map(
                        attr -> {
                            PmsAttrVO vo = convertToVO(attr);
                            return toExcelRow(vo);
                        })
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int importFromExcel(List<PmsAttrExcel> rows, Integer attrType, boolean updateSupport) {
        if (attrType == null) {
            throw new BizException("导入必须指定属性类型");
        }
        assertAttrTypePermission(attrType, "import");
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (PmsAttrExcel row : rows) {
            if (row == null || isBlankAttrRow(row)) {
                continue;
            }
            if (row.getAttrName() == null || row.getAttrName().isBlank()) {
                throw new BizException("导入失败：属性名不能为空");
            }
            if (row.getCatelogId() == null) {
                throw new BizException("导入失败：分类ID不能为空（属性：" + row.getAttrName() + "）");
            }
            PmsAttrDTO dto = new PmsAttrDTO();
            dto.setAttrId(row.getAttrId());
            dto.setAttrName(row.getAttrName().trim());
            dto.setSearchType(row.getSearchType() != null ? row.getSearchType() : 0);
            dto.setValueType(row.getValueType() != null ? row.getValueType() : 0);
            dto.setIcon(row.getIcon());
            dto.setValueSelect(row.getValueSelect());
            dto.setAttrType(attrType);
            dto.setEnable(row.getEnable() != null ? row.getEnable() : 1L);
            dto.setCatelogId(row.getCatelogId());
            dto.setShowDesc(row.getShowDesc() != null ? row.getShowDesc() : 0);
            dto.setAttrGroupId(row.getAttrGroupId());
            dto.setAttrSort(row.getAttrSort());
            if (row.getAttrId() != null && updateSupport) {
                PmsAttr existing = this.getById(row.getAttrId());
                if (existing != null) {
                    if (!Integer.valueOf(attrType).equals(existing.getAttrType())) {
                        throw new BizException("属性ID " + row.getAttrId() + " 类型与当前导入不一致");
                    }
                    if (!updatePmsAttr(dto)) {
                        throw new BizException("更新属性失败，ID=" + row.getAttrId());
                    }
                    count++;
                    continue;
                }
            }
            if (!insertPmsAttr(dto)) {
                throw new BizException("新增属性失败：" + row.getAttrName());
            }
            count++;
        }
        return count;
    }

    private PmsAttrExcel toExcelRow(PmsAttrVO vo) {
        PmsAttrExcel row = new PmsAttrExcel();
        BeanUtils.copyProperties(vo, row);
        return row;
    }

    private boolean isBlankAttrRow(PmsAttrExcel row) {
        return (row.getAttrName() == null || row.getAttrName().isBlank())
                && row.getCatelogId() == null
                && row.getAttrId() == null;
    }
}
