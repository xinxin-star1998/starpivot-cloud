package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeSubjectVo;
import java.util.List;

public interface SmsHomeSubjectService {

    PageResponse<HomeSubjectVo> pageList(HomeSubjectReqBo reqBo);

    HomeSubjectVo getById(Long id);

    void add(HomeSubjectSaveBo bo);

    void update(HomeSubjectSaveBo bo);

    void removeByIds(List<Long> ids);
}
