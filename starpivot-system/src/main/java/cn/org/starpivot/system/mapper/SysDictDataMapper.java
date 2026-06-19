package cn.org.starpivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.org.starpivot.system.domain.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据Mapper接口
 *
 * @author stardust
 * @since 2024-01-01
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictData> selectDictDataByType(@Param("dictType") String dictType);
}