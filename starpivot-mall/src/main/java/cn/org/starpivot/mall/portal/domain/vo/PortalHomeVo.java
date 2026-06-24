package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PortalHomeVo {

    private List<HomeAdvVo> banners;

    private List<CategoryTreeVo> categories;

    /** 一级分类 ID -> 关联品牌（去重，最多 8 个） */
    private Map<Long, List<PortalBrandBriefVo>> categoryBrands;

    /** 首页营销模块：新品 / 秒杀 / 包邮 / 专题 */
    private List<PortalHomeBlockVo> homeBlocks;
}
