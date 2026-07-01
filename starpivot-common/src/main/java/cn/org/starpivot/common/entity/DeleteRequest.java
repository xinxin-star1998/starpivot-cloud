package cn.org.starpivot.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求体，携带待删除记录的主键 ID 列表。
 * <p>
 * 供 Controller 接收前端多选删除参数，与单条 {@code DELETE /{id}} 接口互补。
 * </p>
 */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 待删除记录的主键 ID 集合 */
    private List<Long> ids;

    /** 是否强制删除（跳过关联检查，可选） */
    private Boolean force;
}
