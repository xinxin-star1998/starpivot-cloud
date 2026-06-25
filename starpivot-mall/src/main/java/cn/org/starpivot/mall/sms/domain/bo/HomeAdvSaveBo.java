package cn.org.starpivot.mall.sms.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页广告保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeAdvSaveBo {

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
    @NotBlank(message = "广告名称不能为空")
    @Size(max = 128, message = "广告名称长度不能超过128")
    /**
     * name
     */
    private String name;

    /**
     * pic
     */
    /**
     * pic
     */
    @Size(max = 512, message = "图片地址长度不能超过512")
    /**
     * pic
     */
    private String pic;

    /**
     * start时间
     */
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * end时间
     */
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * click数量
     */
    private Integer clickCount;

    /**
     * url
     */
    /**
     * url
     */
    @Size(max = 512, message = "链接地址长度不能超过512")
    /**
     * url
     */
    private String url;

    /**
     * note
     */
    /**
     * 备注
     */
    @Size(max = 512, message = "备注长度不能超过512")
    /**
     * 备注
     */
    private String note;

    /**
     * 排序
     */
    private Integer sort;
    /**
     * Publisher ID
     */
    private Long publisherId;
    /**
     * Auth ID
     */
    private Long authId;
}
