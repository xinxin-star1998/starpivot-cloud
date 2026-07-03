package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.api.product.dto.CategoryTreeDto;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import cn.org.starpivot.mall.sms.domain.vo.HomeCategoryHotVo;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * Home视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalHomeVo {

    /**
     * banners
     */
    private List<HomeAdvVo> banners;

    /**
     * categories
     */
    private List<CategoryTreeDto> categories;

    /** 一级分类 ID -> 关联品牌（去重，最多 8 个） */
    /**
     * category Brands
     */
    private Map<Long, List<PortalBrandBriefVo>> categoryBrands;

    /** 首页营销模块：新品 / 秒杀 / 包邮 / 专题 */
    /**
     * home Blocks
     */
    private List<PortalHomeBlockVo> homeBlocks;

    /** 首页分类热门 */
    private List<HomeCategoryHotVo> hotCategories;
}
