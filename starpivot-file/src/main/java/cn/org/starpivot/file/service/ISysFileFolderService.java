package cn.org.starpivot.file.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.file.domain.bo.FileCategoryNodeVO;
import cn.org.starpivot.file.domain.dto.SysFileFolderDTO;
import cn.org.starpivot.file.domain.bo.SysFileFolderVO;

import java.util.List;

/**
 * 文件夹服务。
 */
public interface ISysFileFolderService {

    List<FileCategoryNodeVO> listTree(String category);

    Long create(SysFileFolderDTO dto);

    void update(SysFileFolderDTO dto);

    void delete(Long folderId);
}
