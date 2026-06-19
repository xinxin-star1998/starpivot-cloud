package cn.org.starpivot.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用于删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<Long> ids;
}
