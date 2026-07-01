package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberCollectReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSpuVo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSubjectVo;

public interface UmsMemberCollectService {

    PageResponse<MemberCollectSpuVo> spuPageList(MemberCollectReqBo reqBo);

    PageResponse<MemberCollectSubjectVo> subjectPageList(MemberCollectReqBo reqBo);
}
