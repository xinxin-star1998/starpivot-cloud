package cn.org.starpivot.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.job.domain.bo.SysJobLogVO;
import cn.org.starpivot.job.domain.bo.SysJobVO;
import cn.org.starpivot.job.domain.dto.SysJobDTO;
import cn.org.starpivot.job.domain.dto.SysJobLogQueryDTO;
import cn.org.starpivot.job.domain.dto.SysJobQueryDTO;
import cn.org.starpivot.job.domain.entity.SysJob;

import java.util.List;

/**
 * 定时任务 Service 接口
 *
 * @author StarPivot
 */
public interface ISysJobService extends IService<SysJob> {

    PageResponse<SysJobVO> selectJobPage(SysJobQueryDTO query);

    SysJobVO getJobById(Long jobId);

    boolean insertJob(SysJobDTO dto);

    boolean updateJob(SysJobDTO dto);

    boolean deleteJobByIds(List<Long> jobIds);

    boolean changeStatus(Long jobId, String status);

    boolean runOnce(Long jobId);

    void executeJob(Long jobId);

    PageResponse<SysJobLogVO> selectJobLogPage(SysJobLogQueryDTO query);

    boolean clearJobLog();

    void loadScheduledJobsOnStartup();
}
