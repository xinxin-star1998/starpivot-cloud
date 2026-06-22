package cn.org.starpivot.file.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * 左侧 Category + 子文件夹树节点。
 */
@Data
public class FileCategoryNodeVO {

    private String category;

    private String categoryLabel;

    private Long defaultFolderId;

    private List<SysFileFolderVO> children;
}
