package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.SysConfigVO;
import cn.org.starpivot.system.domain.dto.SysConfigDTO;
import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.domain.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysConfigService extends IService<SysConfig> {

    PageResponse<SysConfigVO> selectSysConfigPage(SysConfigQueryDTO queryDTO);

    List<SysConfigVO> selectSysConfigList(SysConfigQueryDTO queryDTO);

    SysConfigVO selectSysConfigByConfigId(Long configId);

    boolean insertSysConfig(SysConfigDTO sysConfigDTO);

    boolean updateSysConfig(SysConfigDTO sysConfigDTO);

    boolean deleteSysConfigByConfigIds(Long[] configIds);

    String selectConfigValueByKey(String configKey);

    boolean isRegisterUserEnabled();
}
