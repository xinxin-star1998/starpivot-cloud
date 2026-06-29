package cn.org.starpivot.file.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.file.domain.bo.SysFileVO;
import cn.org.starpivot.file.domain.dto.SysFileQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileRecycleQueryDTO;
import cn.org.starpivot.file.domain.dto.SysFileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件中心服务。
 */
public interface ISysFileService {

    SysFileVO upload(MultipartFile file, Long folderId, SysFileUploadDTO uploadDTO);

    PageResponse<SysFileVO> pageList(SysFileQueryDTO queryDTO);

    SysFileVO getDetail(Long fileId);

    void logicDelete(List<Long> ids);

    void restore(List<Long> ids);

    PageResponse<SysFileVO> recyclePage(SysFileRecycleQueryDTO queryDTO);

    Map<String, String> previewUrl(Long fileId);

    void moveToFolder(List<Long> ids, Long targetFolderId);

    void rename(Long fileId, String fileName);

    /**
     * 物理清理回收站超期文件，返回实际清理数量。
     */
    int purgeExpiredRecycleFiles();

    /**
     * 回收站永久删除（立即删 OSS + DB）。
     */
    void permanentDelete(List<Long> ids);
}
