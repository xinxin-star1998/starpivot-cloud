package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.SysDictTypeVO;
import cn.org.starpivot.system.domain.dto.SysDictTypeDTO;
import cn.org.starpivot.system.domain.dto.SysDictTypeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysDictType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 字典类型服务接口
 *
 * @author stardust
 * @since 2024-01-01
 */
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 分页查询字典类型列表
     *
     * @param queryDTO 查询条件
     * @return 字典类型分页列表
     */
    PageResponse<SysDictTypeVO> selectDictTypePage(SysDictTypeQueryDTO queryDTO);

    /**
     * 根据字典类型ID查询详情
     *
     * @param dictId 字典类型ID
     * @return 字典类型详情
     */
    SysDictTypeVO selectDictTypeById(Long dictId);

    /**
     * 新增字典类型
     *
     * @param dictTypeDTO 字典类型信息
     * @return 是否成功
     */
    boolean insertDictType(SysDictTypeDTO dictTypeDTO);

    /**
     * 修改字典类型
     *
     * @param dictTypeDTO 字典类型信息
     * @return 是否成功
     */
    boolean updateDictType(SysDictTypeDTO dictTypeDTO);

    /**
     * 删除字典类型（支持单删和批量删除）
     *
     * @param dictIds 字典类型ID列表
     * @return 是否成功
     */
    boolean deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 检查字典类型是否唯一
     *
     * @param dictType 字典类型
     * @param dictId 字典类型ID（修改时传入）
     * @return 是否唯一
     */
    boolean checkDictTypeUnique(String dictType, Long dictId);

    List<SysDictTypeVO> selectList();
}