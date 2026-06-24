package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillSessionSaveBo {

    private Long id;

    @NotBlank(message = "场次名称不能为空")
    @Size(max = 200, message = "场次名称长度不能超过200")
    private String name;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
