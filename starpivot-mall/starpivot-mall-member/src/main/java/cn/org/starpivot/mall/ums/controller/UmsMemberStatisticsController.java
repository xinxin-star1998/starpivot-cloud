package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;
import cn.org.starpivot.mall.ums.service.UmsMemberStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商城-会员统计控制器。
 * <p>
 * 提供商城-会员统计相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/member-statistics}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-会员统计」</li>
 * </ul>
 *
 * @see UmsMemberStatisticsService
 */

@RestController
@RequestMapping("/mall/member-statistics")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员统计")
public class UmsMemberStatisticsController {

    private final UmsMemberStatisticsService umsMemberStatisticsService;

    /**
     * 会员统计分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "会员统计分页列表")
    @PostMapping("/memberStatisticsPageList")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<PageResponse<MemberStatisticsVo>> pageList(@RequestBody MemberStatisticsReqBo reqBo) {
        return Result.success(umsMemberStatisticsService.pageList(reqBo));
    }

    /**
     * 按会员ID查询统计。
     *
     * @param memberId 主键 ID
     * @return 业务数据
     */
    @Operation(summary = "按会员ID查询统计")
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<MemberStatisticsVo> getByMemberId(@PathVariable("memberId") Long memberId) {
        return Result.success(umsMemberStatisticsService.getByMemberId(memberId));
    }

    /**
     * 刷新会员统计（从业务表聚合写入快照）。
     *
     * @param memberId 主键 ID
     * @return 操作结果
     */
    @Log(title = "刷新会员统计", businessType = BusinessType.UPDATE)
    @Operation(summary = "刷新会员统计（从业务表聚合写入快照）")
    @PutMapping("/refresh/{memberId}")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<?> refresh(@PathVariable("memberId") Long memberId) {
        umsMemberStatisticsService.refresh(memberId);
        return Result.success("刷新成功");
    }
}
