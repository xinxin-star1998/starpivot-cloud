package cn.org.starpivot.system.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志分页查询 BO。
 * <p>
 * 继承 {@link PageReqBo}，封装操作日志列表的筛选条件。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 生成 equals/hashCode，包含父类字段</li>
 *   <li>{@link JsonFormat} — 日期时间反序列化格式</li>
 * </ul>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperLogReqBo extends PageReqBo {

    /**
     * 模块标题（模糊匹配）
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 操作人员（模糊匹配）
     */
    private String operName;

    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;

    /**
     * 操作时间范围起始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 操作时间范围结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
