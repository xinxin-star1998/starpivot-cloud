package cn.org.starpivot.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.job.domain.dto.SysJobLogQueryDTO;
import cn.org.starpivot.job.domain.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务日志 Mapper
 *
 * @author StarPivot
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {

    IPage<SysJobLog> selectJobLogPage(Page<SysJobLog> page, @Param("query") SysJobLogQueryDTO query);
}
