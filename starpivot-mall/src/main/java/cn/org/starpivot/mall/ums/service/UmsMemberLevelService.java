package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.mall.ums.domain.bo.MemberLevelSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLevelVo;
import java.util.List;

public interface UmsMemberLevelService {

    List<MemberLevelVo> listAll();

    MemberLevelVo getById(Long id);

    void add(MemberLevelSaveBo bo);

    void update(MemberLevelSaveBo bo);

    void removeByIds(List<Long> ids);
}
