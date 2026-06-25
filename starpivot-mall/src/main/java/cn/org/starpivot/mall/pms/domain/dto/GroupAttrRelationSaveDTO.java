package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * GroupattrrelationsaveDTO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class GroupAttrRelationSaveDTO {

    /**
     * items
     */
    /**
     * items
     */
    @Valid
    /**
     * items
     */
    private List<GroupAttrBindItemDTO> items;

    @Data
    public static class GroupAttrBindItemDTO {

        /**
         * 属性 ID
         */
        /**
         * 属性 ID
         */
        @NotNull(message = "属性id不能为空")
        /**
         * 属性 ID
         */
        private Long attrId;

        /**
         * attr Sort
         */
        private Integer attrSort;
    }
}
