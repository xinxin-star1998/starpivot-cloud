package cn.org.starpivot.mall.sms.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HomeAdvSaveBo {

    private Long id;

    @NotBlank(message = "广告名称不能为空")
    @Size(max = 128, message = "广告名称长度不能超过128")
    private String name;

    @Size(max = 512, message = "图片地址长度不能超过512")
    private String pic;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private Integer status;
    private Integer clickCount;

    @Size(max = 512, message = "链接地址长度不能超过512")
    private String url;

    @Size(max = 512, message = "备注长度不能超过512")
    private String note;

    private Integer sort;
    private Long publisherId;
    private Long authId;
}
