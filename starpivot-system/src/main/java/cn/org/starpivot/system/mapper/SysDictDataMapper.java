package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysDictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据 Mapper 接口。
 * <p>提供按字典类型批量查询字典数据项的自定义 SQL。</p>
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