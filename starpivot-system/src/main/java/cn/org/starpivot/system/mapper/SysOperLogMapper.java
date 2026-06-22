package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysOperLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper 接口。
 * <p>继承 {@link BaseMapper} 提供操作审计日志表基础 CRUD。</p>
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
