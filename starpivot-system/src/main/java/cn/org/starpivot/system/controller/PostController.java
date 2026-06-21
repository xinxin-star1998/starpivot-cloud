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

@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class PostController {

    private final PostService postService;

    @PreAuthorize("hasAuthority('system:post:query')")
    @PostMapping("/list")
    public Result<PageResponse<PostVO>> list(@RequestBody PostQueryDTO queryDTO) {
        return Result.success(postService.selectPostPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/simpleList")
    public Result<List<PostBo>> simpleList() {
        return Result.success(postService.selectPost());
    }

    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/all")
    public Result<List<PostVO>> all() {
        return Result.success(postService.all());
    }

    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/{postId}")
    public Result<PostVO> getInfo(@PathVariable Long postId) {
        return Result.success(postService.selectPostById(postId));
    }

    @Log(title = "新增岗位", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:post:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody PostDTO postDTO) {
        boolean success = postService.insertPost(postDTO);
        return success ? Result.success("新增岗位成功") : Result.error("新增岗位失败");
    }

    @Log(title = "修改岗位", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:post:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody PostDTO postDTO) {
        boolean success = postService.updatePost(postDTO);
        return success ? Result.success("修改岗位成功") : Result.error("修改岗位失败");
    }

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
