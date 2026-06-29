package cn.org.starpivot.file.mapper;

import cn.org.starpivot.file.domain.bo.FolderFileCount;
import cn.org.starpivot.file.domain.dto.SysFileQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileRecycleQueryDTO;
import cn.org.starpivot.file.domain.entity.SysFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 文件元数据 Mapper。
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

    IPage<SysFile> selectPageList(Page<SysFile> page, @Param("query") SysFileQueryDTO query);

    IPage<SysFile> selectRecyclePageList(Page<SysFile> page, @Param("query") SysFileRecycleQueryDTO query);

    /** 按 ID 查询回收站文件（自定义 SQL，与 selectRecyclePageList 条件一致） */
    List<SysFile> selectRecycleByIds(@Param("ids") Collection<Long> ids);

    long countActiveByFolderId(@Param("folderId") Long folderId);

    long countByFolderId(@Param("folderId") Long folderId);

    List<FolderFileCount> countByFolderIds(@Param("folderIds") Collection<Long> folderIds);

    int restoreByIds(@Param("ids") List<Long> ids, @Param("updateBy") String updateBy);

    List<SysFile> selectExpiredRecycleFiles(
            @Param("deadline") LocalDateTime deadline,
            @Param("limit") int limit);

    SysFile selectActiveByHash(@Param("fileHash") String fileHash);

    SysFile selectActiveByObjectName(@Param("objectName") String objectName);

    long countReferencesByObjectName(
            @Param("objectName") String objectName,
            @Param("excludeFileIds") Collection<Long> excludeFileIds);

    int deletePhysicallyByIds(@Param("ids") List<Long> ids);
}
