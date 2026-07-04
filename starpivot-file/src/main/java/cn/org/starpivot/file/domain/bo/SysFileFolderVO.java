package cn.org.starpivot.file.domain.bo;

import lombok.Data;

/**
 * 文件夹树节点。
 */
@Data
public class SysFileFolderVO {

    private Long folderId;

    private String category;

    private String folderName;

    private Long parentId;

    private Integer orderNum;

    private String status;

    private Long fileCount;
}
