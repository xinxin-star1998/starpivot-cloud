package cn.org.starpivot.approval.mapper;

import cn.org.starpivot.approval.domain.entity.ApInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApInstanceMapper extends BaseMapper<ApInstance> {

    @Select("SELECT * FROM ap_instance WHERE instance_id = #{instanceId} FOR UPDATE")
    ApInstance selectByIdForUpdate(@Param("instanceId") Long instanceId);
}
