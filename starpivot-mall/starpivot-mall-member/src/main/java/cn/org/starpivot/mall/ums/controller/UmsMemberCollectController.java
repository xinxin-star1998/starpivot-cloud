package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberCollectReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSpuVo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSubjectVo;
import cn.org.starpivot.mall.ums.service.UmsMemberCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/member-collect")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员收藏")
public class UmsMemberCollectController {

    private final UmsMemberCollectService umsMemberCollectService;

    @Operation(summary = "会员商品收藏分页")
    @PostMapping("/spuPageList")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberCollectSpuVo>> spuPageList(@RequestBody MemberCollectReqBo reqBo) {
        return Result.success(umsMemberCollectService.spuPageList(reqBo));
    }

    @Operation(summary = "会员专题收藏分页")
    @PostMapping("/subjectPageList")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberCollectSubjectVo>> subjectPageList(@RequestBody MemberCollectReqBo reqBo) {
        return Result.success(umsMemberCollectService.subjectPageList(reqBo));
    }
}
