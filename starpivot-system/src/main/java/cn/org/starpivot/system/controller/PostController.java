package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.PostBo;
import cn.org.starpivot.system.domain.bo.PostVO;
import cn.org.starpivot.system.domain.dto.PostDTO;
import cn.org.starpivot.system.domain.dto.PostQueryDTO;
import cn.org.starpivot.system.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理控制器。
 * <p>
 * 提供组织岗位的增删改查及下拉列表查询 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/post}</li>
 *   <li>{@link Tag} — OpenAPI 分组「岗位管理」</li>
 * </ul>
 */
@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class PostController {

    private final PostService postService;

    /**
     * 分页查询岗位列表。
     *
     * @param queryDTO 查询条件
     * @return 岗位分页结果
     */
    @PreAuthorize("hasAuthority('system:post:query')")
    @PostMapping("/list")
    public Result<PageResponse<PostVO>> list(@RequestBody PostQueryDTO queryDTO) {
        return Result.success(postService.selectPostPage(queryDTO));
    }

    /**
     * 查询岗位简要下拉列表。
     *
     * @return 岗位简要信息列表
     */
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/simpleList")
    public Result<List<PostBo>> simpleList() {
        return Result.success(postService.selectPost());
    }

    /**
     * 查询全部岗位（不分页）。
     *
     * @return 岗位视图列表
     */
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/all")
    public Result<List<PostVO>> all() {
        return Result.success(postService.all());
    }

    /**
     * 根据岗位 ID 获取详情。
     *
     * @param postId 岗位主键
     * @return 岗位视图对象
     */
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/{postId}")
    public Result<PostVO> getInfo(@PathVariable Long postId) {
        return Result.success(postService.selectPostById(postId));
    }

    /**
     * 新增岗位。
     *
     * @param postDTO 岗位创建参数
     * @return 操作结果
     */
    @Log(title = "新增岗位", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:post:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody PostDTO postDTO) {
        boolean success = postService.insertPost(postDTO);
        return success ? Result.success("新增岗位成功") : Result.error("新增岗位失败");
    }

    /**
     * 修改岗位信息。
     *
     * @param postDTO 岗位更新参数
     * @return 操作结果
     */
    @Log(title = "修改岗位", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:post:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody PostDTO postDTO) {
        boolean success = postService.updatePost(postDTO);
        return success ? Result.success("修改岗位成功") : Result.error("修改岗位失败");
    }

    /**
     * 批量删除岗位。
     *
     * @param deleteRequest 待删除岗位 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除岗位", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:post:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest.getIds() == null || deleteRequest.getIds().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        boolean success = postService.deletePostByIds(deleteRequest.getIds());
        return success ? Result.success("删除岗位成功") : Result.error("删除岗位失败");
    }
}
