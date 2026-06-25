package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 首页专题保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeSubjectSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "专题名称不能为空")
    @Size(max = 200, message = "专题名称长度不能超过200")
    /**
     * name
     */
    private String name;

    /**
     * title
     */
    /**
     * title
     */
    @Size(max = 255, message = "标题长度不能超过255")
    /**
     * title
     */
    private String title;

    /**
     * sub Title
     */
    /**
     * sub Title
     */
    @Size(max = 255, message = "副标题长度不能超过255")
    /**
     * sub Title
     */
    private String subTitle;

    /**
     * 状态
     */
    private Integer status;
    /**
     * url
     */
    private String url;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图片
     */
    /**
     * img
     */
    @Size(max = 500, message = "图片地址长度不能超过500")
    /**
     * img
     */
    private String img;

    /**
     * spu List
     */
    private List<HomeSubjectSpuBo> spuList;
}
