package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页分类热门实体。
 * <p>
 * 对应数据库表 {@code sms_home_category_hot}。
 * </p>
 */
@Data
@TableName("sms_home_category_hot")
public class SmsHomeCategoryHot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联分类 ID */
    private Long catId;

    /** 展示标题（空则取分类名） */
    private String title;

    /** 展示图标 */
    private String icon;

    /** 跳转链接 */
    private String url;

    /** 0 下线 1 上线 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String note;
}
