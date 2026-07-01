package cn.org.starpivot.mall.ums.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberCollectSpuVo {

    private Long id;

    private Long memberId;

    private String memberUsername;

    private Long spuId;

    private String spuName;

    private String spuImg;

    private LocalDateTime createTime;
}
