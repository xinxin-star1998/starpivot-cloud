package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.entity.PageResponse;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统参数配置服务实现类。
 * <p>
 * 实现 {@link SysConfigService}，含 Redis 缓存配置值及注册开关查询。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 分页查询系统参数配置列表。
     *
     * @param queryDTO 查询条件与分页参数
     * @return {@link SysConfigVO} 分页结果
     */
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

    /**
     * 按条件查询系统参数配置列表（不分页）。
     *
     * @param queryDTO 查询条件
     * @return {@link SysConfigVO} 列表
     */
    @Override
    public List<SysConfigVO> selectSysConfigList(SysConfigQueryDTO queryDTO) {
        return baseMapper.selectListByQuery(queryDTO).stream().map(this::convertToVO).toList();
    }

    /**
     * 根据主键查询参数配置详情。
     *
     * @param configId 参数配置主键
     * @return 参数配置视图对象
     * @throws BizException 配置不存在时抛出
     */
    @Override
    public SysConfigVO selectSysConfigByConfigId(Long configId) {
        SysConfig sysConfig = baseMapper.selectSysConfigByConfigId(configId);
        if (sysConfig == null) {
            throw new BizException("参数配置不存在");
        }
        return convertToVO(sysConfig);
    }

    /**
     * 新增系统参数配置。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param sysConfigDTO 参数配置信息
     * @return 是否新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertSysConfig(SysConfigDTO sysConfigDTO) {
        SysConfig sysConfig = new SysConfig();
        BeanUtils.copyProperties(sysConfigDTO, sysConfig);
        sysConfig.setCreateBy(SecurityContextUtils.getUsername());
        sysConfig.setCreateTime(LocalDateTime.now());
        return baseMapper.insertSysConfig(sysConfig) > 0;
    }

    /**
     * 修改系统参数配置，成功后清除对应 Redis 缓存。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param sysConfigDTO 参数配置信息
     * @return 是否修改成功
     * @throws BizException 配置不存在时抛出
     */
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

    /**
     * 批量删除系统参数配置，删除前清除各配置键的 Redis 缓存。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param configIds 待删除的配置主键数组
     * @return 是否删除成功
     */
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

    /**
     * 根据配置键查询配置值，优先从 Redis 读取，未命中则查库并写入缓存。
     *
     * @param configKey 配置键
     * @return 配置值；键为空或配置不存在时返回 {@code null}
     */
    @Override
    public String selectConfigValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        String cacheKey = CacheConstants.sysConfigKey(configKey);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SysConfig config = baseMapper.selectByConfigKey(configKey);
        if (config == null || config.getConfigValue() == null) {
            return null;
        }
        stringRedisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CacheConstants.TTL_SYS_CONFIG);
        return config.getConfigValue();
    }

    /**
     * 判断系统是否开启用户自助注册。
     *
     * @return 配置值为 {@code true}（忽略大小写）时返回 {@code true}
     */
    @Override
    public boolean isRegisterUserEnabled() {
        return "true".equalsIgnoreCase(StringUtils.trimWhitespace(selectConfigValueByKey(SysConfigKeys.REGISTER_USER)));
    }

    @Override
    public boolean isForgetPasswordEnabled() {
        return "true".equalsIgnoreCase(StringUtils.trimWhitespace(selectConfigValueByKey(SysConfigKeys.FORGET_PASSWORD)));
    }

    /**
     * 将 {@link SysConfig} 实体转换为 {@link SysConfigVO}。
     *
     * @param sysConfig 参数配置实体
     * @return 参数配置视图对象
     */
    private SysConfigVO convertToVO(SysConfig sysConfig) {
        SysConfigVO vo = new SysConfigVO();
        BeanUtils.copyProperties(sysConfig, vo);
        return vo;
    }

    /**
     * 清除指定配置键的 Redis 缓存。
     *
     * @param configKey 配置键
     */
    private void evictConfigCache(String configKey) {
        if (StringUtils.hasText(configKey)) {
            stringRedisTemplate.delete(CacheConstants.sysConfigKey(configKey));
        }
    }
}
