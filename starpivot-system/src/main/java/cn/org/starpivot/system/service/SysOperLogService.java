package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.OperLogReqBo;
import cn.org.starpivot.system.domain.bo.OperLogVO;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 操作日志服务接口。
 * <p>
 * 提供操作审计日志的分页查询能力。
 * </p>
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志。
     *
     * @param operLogReqBo 分页及筛选条件
     * @return 操作日志视图分页结果
     */
    PageResponse<OperLogVO> pageList(OperLogReqBo operLogReqBo);
}
