package cn.org.starpivot.file.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 回收站分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileRecycleQueryDTO extends PageReqBo {

    private String category;

    private String fileName;

    private String deleteBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
