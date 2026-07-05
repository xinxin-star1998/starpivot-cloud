package cn.org.starpivot.tms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_track_event")
public class TmsTrackEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shipmentId;
    private LocalDateTime eventTime;
    private String eventStatus;
    private String eventDesc;
    private String location;
    private String source;
    private String rawJson;
    private LocalDateTime createTime;
}
