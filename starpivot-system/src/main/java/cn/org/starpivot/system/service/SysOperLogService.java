package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.OperLogReqBo;
import cn.org.starpivot.system.domain.bo.OperLogVO;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysOperLogService extends IService<SysOperLog> {

    PageResponse<OperLogVO> pageList(OperLogReqBo operLogReqBo);
}
