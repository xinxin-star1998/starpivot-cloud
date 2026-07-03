package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.api.product.dto.PortalProductListDto;
import cn.org.starpivot.common.entity.PageResponse;
import lombok.Data;

/**
 * C 端专题详情视图对象。
 */
@Data
public class PortalSubjectDetailVo {

    private Long id;

    private String name;

    private String title;

    private String subTitle;

    private String coverImg;

    private String url;

    private PageResponse<PortalProductListDto> products = new PageResponse<>();
}
