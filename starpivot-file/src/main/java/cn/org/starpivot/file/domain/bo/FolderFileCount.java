package cn.org.starpivot.file.domain.bo;

import lombok.Data;

/**
 * 文件夹下文件数量统计（含正常与回收站）。
 */
@Data
public class FolderFileCount {

    private Long folderId;

    private Long fileCount;
}
