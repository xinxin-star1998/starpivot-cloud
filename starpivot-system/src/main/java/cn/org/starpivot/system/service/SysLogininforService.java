package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.LogininforReqBo;
import cn.org.starpivot.system.domain.bo.LogininforVO;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务接口。
 * <p>
 * 提供登录审计日志的保存、分页查询及统计聚合能力。
 * </p>
 */
public interface SysLogininforService extends IService<SysLogininfor> {

    /** 保存一条登录日志记录。 */
    void saveLogininfor(SysLogininfor logininfor);

    /** 分页查询登录日志。 */
    PageResponse<LogininforVO> pageList(LogininforReqBo logininforReqBo);

    /** 按月份统计登录次数（工作台图表用）。 */
    List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end);

    /** 按用户名列表统计指定时间起的登录次数。 */
    List<Map<String, Object>> countByUserNames(List<String> userNames, LocalDateTime start);
}
