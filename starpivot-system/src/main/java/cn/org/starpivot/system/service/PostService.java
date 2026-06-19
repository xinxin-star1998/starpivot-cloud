package cn.org.starpivot.system.service;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.system.domain.bo.PostBo;
import cn.org.starpivot.system.domain.bo.PostVO;
import cn.org.starpivot.system.domain.dto.PostDTO;
import cn.org.starpivot.system.domain.dto.PostQueryDTO;
import cn.org.starpivot.system.domain.entity.SysPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PostService extends IService<SysPost> {

    PageResponse<PostVO> selectPostPage(PostQueryDTO queryDTO);

    PostVO selectPostById(Long postId);

    boolean insertPost(PostDTO postDTO);

    boolean updatePost(PostDTO postDTO);

    boolean deletePostByIds(List<Long> postIds);

    List<PostBo> selectPost();

    List<PostVO> all();
}
