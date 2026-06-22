package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.domain.entity.SysConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统参数配置 Mapper 接口。
 * <p>提供参数配置的分页查询、按 Key 查询及批量删除等自定义 SQL。</p>
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    /** 分页查询参数配置。 */
    IPage<SysConfig> selectPageList(Page<SysConfig> page, @Param("queryDTO") SysConfigQueryDTO queryDTO);

    List<SysConfig> selectListByQuery(@Param("queryDTO") SysConfigQueryDTO queryDTO);

    SysConfig selectSysConfigByConfigId(@Param("configId") Long configId);

    SysConfig selectByConfigKey(@Param("configKey") String configKey);

    int insertSysConfig(SysConfig sysConfig);

    int updateSysConfig(SysConfig sysConfig);

    int deleteSysConfigByConfigIds(@Param("configIds") Long[] configIds);
}
