package cn.org.starpivot.generator.domain.dto.external;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteDiffRequest extends ExternalWriteRequest {

    /** 是否在列表响应中包含文件内容（false 时仅返回 path/status，详情走 write-diff/file） */
    private Boolean includeContent = true;
}

