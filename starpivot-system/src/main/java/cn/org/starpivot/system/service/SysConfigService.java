package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.SysConfigVO;
import cn.org.starpivot.system.domain.dto.SysConfigDTO;
import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.domain.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统参数配置服务接口。
 * <p>
 * 管理系统键值对参数（如注册开关）的增删改查及按 Key 取值。
 * </p>
 */
public interface SysConfigService extends IService<SysConfig> {

    /** 分页查询参数配置。 */
    PageResponse<SysConfigVO> selectSysConfigPage(SysConfigQueryDTO queryDTO);

    /** 查询参数配置列表（不分页）。 */
    List<SysConfigVO> selectSysConfigList(SysConfigQueryDTO queryDTO);

    /** 根据配置 ID 查询详情。 */
    SysConfigVO selectSysConfigByConfigId(Long configId);

    /** 新增参数配置。 */
    boolean insertSysConfig(SysConfigDTO sysConfigDTO);

    /** 修改参数配置。 */
    boolean updateSysConfig(SysConfigDTO sysConfigDTO);

    /** 批量删除参数配置。 */
    boolean deleteSysConfigByConfigIds(Long[] configIds);

    /** 根据配置键获取配置值。 */
    String selectConfigValueByKey(String configKey);

    /** 判断系统是否开放用户自助注册。 */
    boolean isRegisterUserEnabled();
}
