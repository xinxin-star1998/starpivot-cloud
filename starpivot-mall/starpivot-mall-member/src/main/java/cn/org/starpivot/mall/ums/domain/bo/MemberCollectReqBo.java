package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员收藏查询 BO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberCollectReqBo extends PageReqBo {

    private Long memberId;

    private String spuName;

    private String subjectName;
}
