package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.domain.entity.SysConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    IPage<SysConfig> selectPageList(Page<SysConfig> page, @Param("queryDTO") SysConfigQueryDTO queryDTO);

    List<SysConfig> selectListByQuery(@Param("queryDTO") SysConfigQueryDTO queryDTO);

    SysConfig selectSysConfigByConfigId(@Param("configId") Long configId);

    SysConfig selectByConfigKey(@Param("configKey") String configKey);

    int insertSysConfig(SysConfig sysConfig);

    int updateSysConfig(SysConfig sysConfig);

    int deleteSysConfigByConfigIds(@Param("configIds") Long[] configIds);
}
