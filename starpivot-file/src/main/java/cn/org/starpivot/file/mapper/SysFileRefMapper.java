package cn.org.starpivot.file.mapper;

import cn.org.starpivot.file.domain.bo.FileRefCount;
import cn.org.starpivot.file.domain.entity.SysFileRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 文件业务引用 Mapper。
 */
@Mapper
public interface SysFileRefMapper extends BaseMapper<SysFileRef> {

    long countByFileId(@Param("fileId") Long fileId);

    List<FileRefCount> countGroupByFileIds(@Param("fileIds") Collection<Long> fileIds);

    int deleteByFileIds(@Param("fileIds") Collection<Long> fileIds);
}
