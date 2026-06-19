package cn.org.starpivot.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求对象
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 删除的ID列表
     */
    private List<Long> ids;

    /**
     * 是否强制删除（跳过关联检查）
     */
    private Boolean force;
}