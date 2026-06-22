package cn.org.starpivot.file.mapper;

import cn.org.starpivot.file.domain.dto.SysFileQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileRecycleQueryDTO;
import cn.org.starpivot.file.domain.entity.SysFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件元数据 Mapper。
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

    IPage<SysFile> selectPageList(Page<SysFile> page, @Param("query") SysFileQueryDTO query);

    IPage<SysFile> selectRecyclePageList(Page<SysFile> page, @Param("query") SysFileRecycleQueryDTO query);

    long countActiveByFolderId(@Param("folderId") Long folderId);

    int restoreByIds(@Param("ids") List<Long> ids, @Param("updateBy") String updateBy);
}
