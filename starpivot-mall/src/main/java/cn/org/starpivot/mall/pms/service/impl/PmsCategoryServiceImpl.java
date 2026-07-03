package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.cache.SpringCacheSupport;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.CategorySaveBo;
import cn.org.starpivot.mall.pms.domain.bo.CategorySortItemBo;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.pms.entity.PmsCategory;
import cn.org.starpivot.mall.pms.mapper.PmsCategoryMapper;
import cn.org.starpivot.mall.pms.service.PmsCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类。
 * <p>
 * 实现 {@link PmsCategoryService}，处理商品分类相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 * </ul>
 *
 * @see PmsCategoryService
 */

@Service
@RequiredArgsConstructor
public class PmsCategoryServiceImpl extends ServiceImpl<PmsCategoryMapper, PmsCategory> implements PmsCategoryService {

    private static final String CACHE_TREE = "mallCategoryTree";
    private static final String CACHE_CHILDREN = "mallCategoryChildren";
    private static final String CACHE_KEY_TREE_ALL = "all";

    private final SpringCacheSupport springCacheSupport;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_TREE, key = "'all'")
    public List<CategoryTreeVo> treeList() {
        List<PmsCategory> all =
                list(Wrappers.<PmsCategory>lambdaQuery()
                        .orderByAsc(PmsCategory::getSort)
                        .orderByAsc(PmsCategory::getCatId));
        return buildTree(all);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CACHE_CHILDREN,
            key = "#parentCid == null || #parentCid == 0L ? '0' : #parentCid.toString()")
    public List<CategoryTreeVo> listChildren(Long parentCid) {
        LambdaQueryWrapper<PmsCategory> q =
                Wrappers.<PmsCategory>lambdaQuery()
                        .orderByAsc(PmsCategory::getSort)
                        .orderByAsc(PmsCategory::getCatId);
        if (parentCid == null || parentCid == 0L) {
            q.and(
                    w ->
                            w.isNull(PmsCategory::getParentCid)
                                    .or()
                                    .eq(PmsCategory::getParentCid, 0L));
        } else {
            q.eq(PmsCategory::getParentCid, parentCid);
        }
        return list(q).stream().map(PmsCategoryServiceImpl::toVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryTreeVo getDetail(Long catId) {
        if (catId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分类ID不能为空");
        }
        PmsCategory c = getById(catId);
        if (c == null) {
            throw new BizException("分类不存在");
        }
        return toVo(c);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(CategorySaveBo bo) {
        if (bo.getCatId() != null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "新增时不要传分类ID");
        }
        Long parentCid =
                bo.getParentCid() == null || bo.getParentCid() == 0L ? 0L : bo.getParentCid();
        long catLevel;
        if (parentCid == 0L) {
            catLevel = 1L;
        } else {
            PmsCategory parent = getById(parentCid);
            if (parent == null) {
                throw new BizException("父分类不存在");
            }
            long pl = parent.getCatLevel() == null ? 1L : parent.getCatLevel();
            if (pl >= 3L) {
                throw new BizException("三级类目下不能再新增子分类");
            }
            catLevel = pl + 1L;
        }
        PmsCategory entity = new PmsCategory();
        entity.setName(bo.getName());
        entity.setParentCid(parentCid);
        entity.setCatLevel(catLevel);
        entity.setShowStatus(
                bo.getShowStatus() == null ? null : Long.valueOf(bo.getShowStatus().longValue()));
        entity.setSort(bo.getSort() == null ? 0L : Long.valueOf(bo.getSort().longValue()));
        entity.setIcon(bo.getIcon());
        entity.setProductUnit(bo.getProductUnit());
        entity.setProductCount(0L);
        save(entity);
        evictAfterCategoryChange(parentCid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategorySaveBo bo) {
        if (bo.getCatId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改时分类ID不能为空");
        }
        PmsCategory existing = getById(bo.getCatId());
        if (existing == null) {
            throw new BizException("分类不存在");
        }
        existing.setName(bo.getName());
        existing.setShowStatus(
                bo.getShowStatus() == null ? null : Long.valueOf(bo.getShowStatus().longValue()));
        existing.setSort(
                bo.getSort() == null
                        ? (existing.getSort() == null ? 0L : existing.getSort())
                        : Long.valueOf(bo.getSort().longValue()));
        existing.setIcon(bo.getIcon());
        existing.setProductUnit(bo.getProductUnit());
        updateById(existing);
        evictAfterCategoryChange(normalizeParentCid(existing.getParentCid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        Set<Long> parentIds = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            long cnt =
                    count(
                            Wrappers.<PmsCategory>lambdaQuery()
                                    .eq(PmsCategory::getParentCid, id));
            if (cnt > 0) {
                throw new BizException("存在子分类，请先删除子分类后再删除当前分类");
            }
            PmsCategory existing = getById(id);
            if (existing != null) {
                parentIds.add(normalizeParentCid(existing.getParentCid()));
            }
            evictCategoryChildren(id);
        }
        super.removeByIds(ids);
        evictCategoryTree();
        parentIds.forEach(this::evictCategoryChildren);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortBatch(List<CategorySortItemBo> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "排序项不能为空");
        }
        Long expectedParent = null;
        List<PmsCategory> updates = new ArrayList<>(items.size());
        for (CategorySortItemBo item : items) {
            if (item.getCatId() == null || item.getSort() == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "分类ID与排序值不能为空");
            }
            PmsCategory entity = getById(item.getCatId());
            if (entity == null) {
                throw new BizException("分类不存在: " + item.getCatId());
            }
            Long parent = normalizeParentCid(entity.getParentCid());
            if (expectedParent == null) {
                expectedParent = parent;
            } else if (!Objects.equals(expectedParent, parent)) {
                throw new BizException("仅允许对同一父级下的分类进行排序");
            }
            entity.setSort(item.getSort().longValue());
            updates.add(entity);
        }
        updateBatchById(updates);
        evictCategoryTree();
        if (expectedParent != null) {
            evictCategoryChildren(expectedParent);
        }
    }

    private void evictAfterCategoryChange(Long parentCid) {
        evictCategoryTree();
        evictCategoryChildren(parentCid);
    }

    private void evictCategoryTree() {
        springCacheSupport.evict(CACHE_TREE, CACHE_KEY_TREE_ALL);
    }

    private void evictCategoryChildren(Long parentCid) {
        springCacheSupport.evict(CACHE_CHILDREN, childrenCacheKey(parentCid));
    }

    private static String childrenCacheKey(Long parentCid) {
        Long normalized = normalizeParentCid(parentCid);
        return normalized == 0L ? "0" : normalized.toString();
    }

    private static List<CategoryTreeVo> buildTree(List<PmsCategory> all) {
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, CategoryTreeVo> byId = new HashMap<>(all.size() * 2);
        for (PmsCategory c : all) {
            CategoryTreeVo vo = toVo(c);
            vo.setChildren(new ArrayList<>());
            byId.put(c.getCatId(), vo);
        }
        List<CategoryTreeVo> roots = new ArrayList<>();
        for (PmsCategory c : all) {
            CategoryTreeVo vo = byId.get(c.getCatId());
            if (isRootCategory(c)) {
                roots.add(vo);
            } else {
                Long pid = c.getParentCid();
                CategoryTreeVo parent = pid == null ? null : byId.get(pid);
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        Comparator<CategoryTreeVo> bySortThenId =
                Comparator.comparing((CategoryTreeVo v) -> v.getSort() == null ? 0L : v.getSort())
                        .thenComparing(v -> v.getCatId() == null ? 0L : v.getCatId());
        sortTreeLevels(roots, bySortThenId);
        return roots;
    }

    private static void sortTreeLevels(
            List<CategoryTreeVo> roots, Comparator<CategoryTreeVo> bySortThenId) {
        Deque<List<CategoryTreeVo>> queue = new ArrayDeque<>();
        queue.add(roots);
        while (!queue.isEmpty()) {
            List<CategoryTreeVo> siblings = queue.poll();
            siblings.sort(bySortThenId);
            for (CategoryTreeVo node : siblings) {
                List<CategoryTreeVo> children = node.getChildren();
                if (children != null && !children.isEmpty()) {
                    queue.add(children);
                }
            }
        }
    }

    private static boolean isRootCategory(PmsCategory c) {
        Long pid = c.getParentCid();
        return pid == null || pid == 0L;
    }

    private static Long normalizeParentCid(Long parentCid) {
        return parentCid == null || parentCid == 0L ? 0L : parentCid;
    }

    private static CategoryTreeVo toVo(PmsCategory c) {
        CategoryTreeVo vo = new CategoryTreeVo();
        vo.setCatId(c.getCatId());
        vo.setName(c.getName());
        vo.setParentCid(c.getParentCid());
        vo.setCatLevel(c.getCatLevel());
        vo.setShowStatus(c.getShowStatus());
        vo.setSort(c.getSort());
        vo.setIcon(c.getIcon());
        vo.setProductUnit(c.getProductUnit());
        vo.setProductCount(c.getProductCount());
        return vo;
    }
}
