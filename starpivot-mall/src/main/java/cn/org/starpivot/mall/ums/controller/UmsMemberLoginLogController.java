package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberLoginLogReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLoginLogVo;
import cn.org.starpivot.mall.ums.service.UmsMemberLoginLogService;
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
@RequestMapping("/mall/member-login-log")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员登录日志")
public class UmsMemberLoginLogController {

    private final UmsMemberLoginLogService umsMemberLoginLogService;

    @Operation(summary = "会员登录日志分页")
    @PostMapping("/loginLogPageList")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberLoginLogVo>> pageList(@RequestBody MemberLoginLogReqBo reqBo) {
        return Result.success(umsMemberLoginLogService.pageList(reqBo));
    }
}
