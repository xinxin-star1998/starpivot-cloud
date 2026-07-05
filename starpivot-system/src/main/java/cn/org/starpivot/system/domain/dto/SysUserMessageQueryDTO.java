package cn.org.starpivot.system.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessageQueryDTO extends PageReqBo {

    /** 0未读 1已读，空则全部 */
    private String readFlag;

    /** 消息类型筛选 */
    private String msgType;
}
