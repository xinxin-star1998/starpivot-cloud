package cn.org.starpivot.mall.pms.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** SPU 详情 VO（含发布向导关联数据） */
@Data
public class ProductVo {

    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private Integer publishStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 商品详情图 URL 列表 */
    private List<String> decript;

    /** SPU 图集 URL 列表 */
    private List<String> images;

    /** 列表展示用封面（图集中默认图或首张） */
    private String coverImg;

    private List<BaseAttrs> baseAttrs;

    private List<Skus> skus;
}
