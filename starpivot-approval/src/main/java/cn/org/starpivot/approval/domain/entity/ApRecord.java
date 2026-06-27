package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_record")
public class ApRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;
    private Long instanceId;
    private Long taskId;
    private String stepCode;
    private String stepName;
    private Long operatorId;
    private String action;
    private String comment;
    private LocalDateTime createTime;
}
