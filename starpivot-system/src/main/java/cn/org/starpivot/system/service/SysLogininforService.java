package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.LogininforReqBo;
import cn.org.starpivot.system.domain.bo.LogininforVO;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SysLogininforService extends IService<SysLogininfor> {

    void saveLogininfor(SysLogininfor logininfor);

    PageResponse<LogininforVO> pageList(LogininforReqBo logininforReqBo);

    List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end);

    List<Map<String, Object>> countByUserNames(List<String> userNames, LocalDateTime start);
}
