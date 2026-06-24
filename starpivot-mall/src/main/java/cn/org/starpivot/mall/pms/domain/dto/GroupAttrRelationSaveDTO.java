package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量保存分组与基本属性的关联。
 */
@Data
public class GroupAttrRelationSaveDTO {

    @Valid
    private List<GroupAttrBindItemDTO> items;

    @Data
    public static class GroupAttrBindItemDTO {

        @NotNull(message = "属性id不能为空")
        private Long attrId;

        private Integer attrSort;
    }
}
