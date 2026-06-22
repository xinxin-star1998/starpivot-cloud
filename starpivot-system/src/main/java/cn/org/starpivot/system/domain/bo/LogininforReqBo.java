package cn.org.starpivot.system.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询 BO。
 * <p>
 * 继承 {@link PageReqBo}，封装登录日志列表的筛选条件。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 生成 equals/hashCode，包含父类字段</li>
 *   <li>{@link JsonFormat} — 日期时间反序列化格式</li>
 * </ul>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogininforReqBo extends PageReqBo {

    /**
     * 用户账号（模糊匹配）
     */
    private String userName;

    /**
     * 登录IP地址（模糊匹配）
     */
    private String ipaddr;

    /**
     * 登录状态（0成功 1失败）
     */
    private String status;

    /**
     * 访问时间范围起始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 访问时间范围结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
