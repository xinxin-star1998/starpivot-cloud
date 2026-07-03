package cn.org.starpivot.common.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量预签名 URL 请求。
 */
@Data
public class PresignedUrlsBo {

    @NotNull(message = "对象路径列表不能为 null")
    @Size(max = 100, message = "单次最多查询100个对象")
    private List<String> objectNames;
}
