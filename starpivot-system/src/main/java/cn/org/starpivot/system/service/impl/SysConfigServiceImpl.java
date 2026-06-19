package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.constants.SysConfigKeys;
import cn.org.starpivot.system.domain.bo.SysConfigVO;
import cn.org.starpivot.system.domain.dto.SysConfigDTO;
import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.domain.entity.SysConfig;
import cn.org.starpivot.system.mapper.SysConfigMapper;
import cn.org.starpivot.system.service.SysConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private static final String CONFIG_CACHE_PREFIX = "sys_config:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResponse<SysConfigVO> selectSysConfigPage(SysConfigQueryDTO queryDTO) {
        Page<SysConfig> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysConfig> sysConfigPage = baseMapper.selectPageList(page, queryDTO);
        List<SysConfigVO> voList = sysConfigPage.getRecords().stream().map(this::convertToVO).toList();

        PageResponse<SysConfigVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(sysConfigPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(sysConfigPage.getCurrent());
        pageResponse.setPageSize(sysConfigPage.getSize());
        pageResponse.setPageCount(sysConfigPage.getPages());
        return pageResponse;
    }

    @Override
    public List<SysConfigVO> selectSysConfigList(SysConfigQueryDTO queryDTO) {
        return baseMapper.selectListByQuery(queryDTO).stream().map(this::convertToVO).toList();
    }

    @Override
    public SysConfigVO selectSysConfigByConfigId(Long configId) {
        SysConfig sysConfig = baseMapper.selectSysConfigByConfigId(configId);
        if (sysConfig == null) {
            throw new BizException("参数配置不存在");
        }
        return convertToVO(sysConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertSysConfig(SysConfigDTO sysConfigDTO) {
        SysConfig sysConfig = new SysConfig();
        BeanUtils.copyProperties(sysConfigDTO, sysConfig);
        sysConfig.setCreateBy(SecurityContextUtils.getUsername());
        sysConfig.setCreateTime(LocalDateTime.now());
        return baseMapper.insertSysConfig(sysConfig) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSysConfig(SysConfigDTO sysConfigDTO) {
        SysConfig sysConfig = baseMapper.selectSysConfigByConfigId(sysConfigDTO.getConfigId());
        if (sysConfig == null) {
            throw new BizException("参数配置不存在");
        }
        String oldConfigKey = sysConfig.getConfigKey();
        BeanUtils.copyProperties(sysConfigDTO, sysConfig, "configId");
        sysConfig.setUpdateBy(SecurityContextUtils.getUsername());
        sysConfig.setUpdateTime(LocalDateTime.now());
        boolean success = baseMapper.updateSysConfig(sysConfig) > 0;
        if (success) {
            evictConfigCache(sysConfig.getConfigKey());
            if (StringUtils.hasText(oldConfigKey) && !oldConfigKey.equals(sysConfig.getConfigKey())) {
                evictConfigCache(oldConfigKey);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSysConfigByConfigIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfig config = baseMapper.selectSysConfigByConfigId(configId);
            if (config != null) {
                evictConfigCache(config.getConfigKey());
            }
        }
        return baseMapper.deleteSysConfigByConfigIds(configIds) > 0;
    }

    @Override
    public String selectConfigValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SysConfig config = baseMapper.selectByConfigKey(configKey);
        if (config == null || config.getConfigValue() == null) {
            return null;
        }
        stringRedisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), Duration.ofHours(2));
        return config.getConfigValue();
    }

    @Override
    public boolean isRegisterUserEnabled() {
        return "true".equalsIgnoreCase(StringUtils.trimWhitespace(selectConfigValueByKey(SysConfigKeys.REGISTER_USER)));
    }

    private SysConfigVO convertToVO(SysConfig sysConfig) {
        SysConfigVO vo = new SysConfigVO();
        BeanUtils.copyProperties(sysConfig, vo);
        return vo;
    }

    private void evictConfigCache(String configKey) {
        if (StringUtils.hasText(configKey)) {
            stringRedisTemplate.delete(CONFIG_CACHE_PREFIX + configKey);
        }
    }
}
