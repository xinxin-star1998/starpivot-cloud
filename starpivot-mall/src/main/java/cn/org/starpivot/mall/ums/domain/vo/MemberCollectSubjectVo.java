package cn.org.starpivot.mall.ums.domain.vo;

import lombok.Data;

@Data
public class MemberCollectSubjectVo {

    private Long id;

    private Long memberId;

    private String memberUsername;

    private Long subjectId;

    private String subjectName;

    private String subjectImg;

    private String subjectUrl;
}
