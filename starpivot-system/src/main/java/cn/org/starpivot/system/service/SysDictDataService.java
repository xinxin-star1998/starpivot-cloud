package cn.org.starpivot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.system.domain.bo.SysDictDataVO;
import cn.org.starpivot.system.domain.dto.SysDictDataDTO;
import cn.org.starpivot.system.domain.dto.SysDictDataQueryDTO;
import cn.org.starpivot.system.domain.entity.SysDictData;
import cn.org.starpivot.common.domain.PageResponse;

import java.util.List;

/**
 * 字典数据服务接口
 *
 * @author stardust
 * @since 2024-01-01
 */
public interface SysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据列表
     *
     * @param queryDTO 查询条件
     * @return 字典数据分页列表
     */
    PageResponse<SysDictDataVO> selectDictDataPage(SysDictDataQueryDTO queryDTO);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictDataVO> selectDictDataByType(String dictType);

    /**
     * 根据字典编码查询字典数据详情
     *
     * @param dictCode 字典编码
     * @return 字典数据详情
     */
    SysDictDataVO selectDictDataById(Long dictCode);

    /**
     * 新增字典数据
     *
     * @param dictDataDTO 字典数据信息
     * @return 是否成功
     */
    boolean insertDictData(SysDictDataDTO dictDataDTO);

    /**
     * 修改字典数据
     *
     * @param dictDataDTO 字典数据信息
     * @return 是否成功
     */
    boolean updateDictData(SysDictDataDTO dictDataDTO);

    /**
     * 删除字典数据（支持单删和批量删除）
     *
     * @param dictCodes 字典编码列表
     * @return 是否成功
     */
    boolean deleteDictDataByIds(List<Long> dictCodes);
}