package cn.org.starpivot.system.service;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.system.domain.bo.LogininforReqBo;
import cn.org.starpivot.system.domain.bo.LogininforVO;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysLogininforService extends IService<SysLogininfor> {

    void saveLogininfor(SysLogininfor logininfor);

    PageResponse<LogininforVO> pageList(LogininforReqBo logininforReqBo);
}
