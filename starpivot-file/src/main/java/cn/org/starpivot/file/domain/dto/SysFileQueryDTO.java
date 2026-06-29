package cn.org.starpivot.file.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件列表分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileQueryDTO extends PageReqBo {

    private Long folderId;

    private String category;

    private String mediaType;

    private String fileName;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /** 服务层注入：当前用户可访问的 Category 列表 */
    @JsonIgnore
    private List<String> accessibleCategories;
}
