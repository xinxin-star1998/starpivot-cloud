package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    List<Long> selectAllDeptIds();
}