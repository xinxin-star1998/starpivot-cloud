package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillSessionVo;
import java.util.List;

public interface SmsSeckillSessionService {

    PageResponse<SeckillSessionVo> pageList(SeckillSessionReqBo reqBo);

    List<SeckillSessionVo> listAll();

    SeckillSessionVo getById(Long id);

    void add(SeckillSessionSaveBo bo);

    void update(SeckillSessionSaveBo bo);

    void removeByIds(List<Long> ids);
}
