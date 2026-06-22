package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.PostBo;
import cn.org.starpivot.system.domain.bo.PostVO;
import cn.org.starpivot.system.domain.dto.PostDTO;
import cn.org.starpivot.system.domain.dto.PostQueryDTO;
import cn.org.starpivot.system.domain.entity.SysPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 岗位管理服务接口。
 * <p>
 * 提供组织岗位的增删改查及下拉列表查询。
 * </p>
 */
public interface PostService extends IService<SysPost> {

    /** 分页查询岗位列表。 */
    PageResponse<PostVO> selectPostPage(PostQueryDTO queryDTO);

    /** 根据岗位 ID 查询详情。 */
    PostVO selectPostById(Long postId);

    /** 新增岗位。 */
    boolean insertPost(PostDTO postDTO);

    /** 修改岗位信息。 */
    boolean updatePost(PostDTO postDTO);

    /** 批量删除岗位。 */
    boolean deletePostByIds(List<Long> postIds);

    /** 查询岗位简要下拉列表。 */
    List<PostBo> selectPost();

    /** 查询全部岗位（不分页）。 */
    List<PostVO> all();
}
