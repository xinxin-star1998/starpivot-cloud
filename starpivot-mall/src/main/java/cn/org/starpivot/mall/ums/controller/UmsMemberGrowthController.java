package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberGrowthReqBo;
import cn.org.starpivot.mall.ums.domain.vo.GrowthChangeHistoryVo;
import cn.org.starpivot.mall.ums.domain.vo.IntegrationChangeHistoryVo;
import cn.org.starpivot.mall.ums.service.UmsMemberGrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商城-会员积分成长控制器。
 * <p>
 * 提供商城-会员积分成长相关 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/member-growth}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-会员积分成长」</li>
 * </ul>
 *
 * @see UmsMemberGrowthService
 */

@RestController
@RequestMapping("/mall/member-growth")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员积分成长")
public class UmsMemberGrowthController {

    private final UmsMemberGrowthService umsMemberGrowthService;

    /**
     * 积分变动记录分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "积分变动记录分页列表")
    @PostMapping("/integrationPageList")
    @PreAuthorize("hasAuthority('mall:member:growth')")
    public Result<PageResponse<IntegrationChangeHistoryVo>> integrationPageList(
            @RequestBody MemberGrowthReqBo reqBo) {
        return Result.success(umsMemberGrowthService.integrationPageList(reqBo));
    }

    /**
     * 成长值变动记录分页列表。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页查询结果
     */
    @Operation(summary = "成长值变动记录分页列表")
    @PostMapping("/growthPageList")
    @PreAuthorize("hasAuthority('mall:member:growth')")
    public Result<PageResponse<GrowthChangeHistoryVo>> growthPageList(@RequestBody MemberGrowthReqBo reqBo) {
        return Result.success(umsMemberGrowthService.growthPageList(reqBo));
    }
}
