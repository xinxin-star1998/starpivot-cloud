package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.domain.bo.CommentShowStatusBo;
import cn.org.starpivot.mall.pms.domain.vo.CommentVo;
import cn.org.starpivot.mall.pms.service.PmsSpuCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-评论控制器。
 * <p>
 * 商品评论管理（表 pms_spu_comment）。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/comment}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-评论」</li>
 * </ul>
 *
 * @see PmsSpuCommentService
 */

@RestController
@RequestMapping("/mall/comment")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-评论", description = "商品评论管理（表 pms_spu_comment）")
public class PmsSpuCommentController {

    private final PmsSpuCommentService pmsSpuCommentService;

    /**
     * 评论分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "评论分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:comment:list')")
    public Result<PageResponse<CommentVo>> pageList(@RequestBody CommentReqBo reqBo) {
        return Result.success(pmsSpuCommentService.pageList(reqBo));
    }

    /**
     * 评论详情。
     *
     * @param id 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "评论详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:comment:query')")
    public Result<CommentVo> getById(@PathVariable("id") Long id) {
        return Result.success(pmsSpuCommentService.getById(id));
    }

    /**
     * 评论展示/隐藏。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Log(title = "评论展示状态", businessType = BusinessType.UPDATE)
    @Operation(summary = "评论展示/隐藏", description = "showStatus：0-隐藏 1-显示")
    @PutMapping("/show-status")
    @PreAuthorize("hasAuthority('mall:comment:edit')")
    public Result<?> updateShowStatus(@Valid @RequestBody CommentShowStatusBo bo) {
        pmsSpuCommentService.updateShowStatus(bo.getId(), bo.getShowStatus());
        return Result.success(bo.getShowStatus() == 1 ? "已设为显示" : "已设为隐藏");
    }

    /**
     * 删除评论。
     *
     * @param deleteRequest 待删除主键 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除评论", businessType = BusinessType.DELETE)
    @Operation(summary = "删除评论", description = "请求体 ids 为评论主键列表")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:comment:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        pmsSpuCommentService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
