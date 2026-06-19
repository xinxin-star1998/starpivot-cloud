package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.bo.PostBo;
import cn.org.starpivot.system.domain.bo.PostVO;
import cn.org.starpivot.system.domain.dto.PostDTO;
import cn.org.starpivot.system.domain.dto.PostQueryDTO;
import cn.org.starpivot.system.domain.entity.SysPost;
import cn.org.starpivot.system.domain.entity.UserPost;
import cn.org.starpivot.system.mapper.PostMapper;
import cn.org.starpivot.system.mapper.UserPostMapper;
import cn.org.starpivot.system.service.PostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, SysPost> implements PostService {

    private final UserPostMapper userPostMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostVO> selectPostPage(PostQueryDTO queryDTO) {
        Page<SysPost> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getPostCode()), SysPost::getPostCode, queryDTO.getPostCode())
                .like(StringUtils.hasText(queryDTO.getPostName()), SysPost::getPostName, queryDTO.getPostName())
                .eq(StringUtils.hasText(queryDTO.getStatus()), SysPost::getStatus, queryDTO.getStatus())
                .orderByAsc(SysPost::getPostSort);
        IPage<SysPost> postPage = this.page(page, wrapper);

        PageResponse<PostVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(postPage.getTotal());
        pageResponse.setRows(postPage.getRecords().stream().map(this::convertToVO).toList());
        pageResponse.setPageNum(Long.valueOf(queryDTO.getPageNum()));
        pageResponse.setPageSize(Long.valueOf(queryDTO.getPageSize()));
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public PostVO selectPostById(Long postId) {
        SysPost post = this.getById(postId);
        AssertUtils.notNull(post, ErrorCode.POST_NOT_FOUND);
        return convertToVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertPost(PostDTO postDTO) {
        if (!checkPostCodeUnique(postDTO.getPostCode(), null)) {
            throw new BizException(ErrorCode.POST_CODE_EXISTS, "岗位编码已存在");
        }
        SysPost post = new SysPost();
        BeanUtils.copyProperties(postDTO, post);
        post.setPostSort(postDTO.getPostSort() != null ? postDTO.getPostSort() : 0);
        post.setStatus(StringUtils.hasText(postDTO.getStatus()) ? postDTO.getStatus() : "0");
        post.setCreateBy(SecurityContextUtils.getUsername());
        post.setCreateTime(LocalDateTime.now());
        return this.save(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePost(PostDTO postDTO) {
        SysPost post = this.getById(postDTO.getPostId());
        AssertUtils.notNull(post, ErrorCode.POST_NOT_FOUND);
        if (!checkPostCodeUnique(postDTO.getPostCode(), postDTO.getPostId())) {
            throw new BizException(ErrorCode.POST_CODE_USED, "岗位编码已被使用");
        }
        BeanUtils.copyProperties(postDTO, post, "postId");
        post.setUpdateBy(SecurityContextUtils.getUsername());
        post.setUpdateTime(LocalDateTime.now());
        return this.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePostByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return false;
        }
        for (Long postId : postIds) {
            SysPost post = this.getById(postId);
            if (post == null) {
                continue;
            }
            LambdaQueryWrapper<UserPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPost::getPostId, postId);
            if (userPostMapper.selectCount(wrapper) > 0) {
                throw new BizException(ErrorCode.POST_USED, "岗位[" + post.getPostName() + "]已被使用，不能删除");
            }
            this.removeById(postId);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostBo> selectPost() {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getStatus, "0").orderByAsc(SysPost::getPostSort);
        return this.list(wrapper).stream().map(post -> {
            PostBo bo = new PostBo();
            BeanUtils.copyProperties(post, bo);
            return bo;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostVO> all() {
        return this.list(new LambdaQueryWrapper<SysPost>().orderByAsc(SysPost::getPostSort))
                .stream().map(this::convertToVO).toList();
    }

    private boolean checkPostCodeUnique(String postCode, Long postId) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getPostCode, postCode);
        if (postId != null) {
            wrapper.ne(SysPost::getPostId, postId);
        }
        return this.count(wrapper) == 0;
    }

    private PostVO convertToVO(SysPost post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
