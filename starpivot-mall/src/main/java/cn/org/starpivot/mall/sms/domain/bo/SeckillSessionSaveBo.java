package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀场次保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SeckillSessionSaveBo {

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
    @NotBlank(message = "场次名称不能为空")
    @Size(max = 200, message = "场次名称长度不能超过200")
    /**
     * name
     */
    private String name;

    /**
     * start时间
     */
    private LocalDateTime startTime;
    /**
     * end时间
     */
    private LocalDateTime endTime;
    /**
     * 状态
     */
    private Integer status;
}
