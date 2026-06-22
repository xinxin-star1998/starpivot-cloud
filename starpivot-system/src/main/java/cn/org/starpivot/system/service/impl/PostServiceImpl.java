package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
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

/**
 * 岗位管理服务实现类。
 * <p>实现 {@link PostService}，含岗位 CRUD 及与用户关联的删除校验。</p>
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, SysPost> implements PostService {

    private final UserPostMapper userPostMapper;

    /**
     * 分页查询岗位列表，支持按编码、名称、状态筛选。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param queryDTO 查询条件与分页参数
     * @return {@link PostVO} 分页结果
     */
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

    /**
     * 根据主键查询岗位详情。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param postId 岗位主键
     * @return 岗位视图对象
     * @throws cn.org.starpivot.common.exception.BizException 岗位不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public PostVO selectPostById(Long postId) {
        SysPost post = this.getById(postId);
        AssertUtils.notNull(post, ErrorCode.POST_NOT_FOUND);
        return convertToVO(post);
    }

    /**
     * 新增岗位，校验岗位编码唯一性。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param postDTO 岗位信息
     * @return 是否新增成功
     * @throws BizException 岗位编码已存在时抛出
     */
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

    /**
     * 修改岗位信息，校验岗位编码唯一性。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param postDTO 岗位信息
     * @return 是否修改成功
     * @throws cn.org.starpivot.common.exception.BizException 岗位不存在或编码已被使用时抛出
     */
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

    /**
     * 批量删除岗位，已分配用户的岗位不允许删除。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)}，异常时回滚事务。</p>
     *
     * @param postIds 待删除的岗位主键列表
     * @return 删除成功返回 {@code true}；列表为空返回 {@code false}
     * @throws BizException 岗位已被用户使用时抛出
     */
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

    /**
     * 查询所有正常状态的岗位，供下拉选择使用。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @return {@link PostBo} 列表，按排序号升序
     */
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

    /**
     * 查询全部岗位（含停用），返回完整视图对象列表。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @return {@link PostVO} 列表，按排序号升序
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostVO> all() {
        return this.list(new LambdaQueryWrapper<SysPost>().orderByAsc(SysPost::getPostSort))
                .stream().map(this::convertToVO).toList();
    }

    /**
     * 校验岗位编码是否唯一。
     *
     * @param postCode 岗位编码
     * @param postId   当前岗位主键；新增时传 {@code null}，修改时用于排除自身
     * @return 编码唯一返回 {@code true}
     */
    private boolean checkPostCodeUnique(String postCode, Long postId) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getPostCode, postCode);
        if (postId != null) {
            wrapper.ne(SysPost::getPostId, postId);
        }
        return this.count(wrapper) == 0;
    }

    /**
     * 将 {@link SysPost} 实体转换为 {@link PostVO}。
     *
     * @param post 岗位实体
     * @return 岗位视图对象
     */
    private PostVO convertToVO(SysPost post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
