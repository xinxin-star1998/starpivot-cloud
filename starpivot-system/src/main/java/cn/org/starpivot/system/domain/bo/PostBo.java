package cn.org.starpivot.system.domain.bo;

import lombok.Data;

/**
 * 岗位简要 BO。
 * <p>
 * 用于下拉选择等场景，仅包含岗位核心标识字段。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class PostBo {

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;
}
