package cn.org.starpivot.file.domain.entity;

import cn.org.starpivot.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文件中心文件夹 sys_file_folder。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_folder")
public class SysFileFolder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "folder_id", type = IdType.AUTO)
    private Long folderId;

    private String category;

    private String folderName;

    private Long parentId;

    private Integer orderNum;

    private String status;

    @TableLogic
    private String delFlag;
}
