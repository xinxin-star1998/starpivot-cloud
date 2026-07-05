package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberReceiveAddressReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberReceiveAddressVo;
import cn.org.starpivot.mall.ums.service.UmsMemberReceiveAddressService;
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
@RequestMapping("/mall/member-receive-address")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员收货地址")
public class UmsMemberReceiveAddressController {

    private final UmsMemberReceiveAddressService umsMemberReceiveAddressService;

    @Operation(summary = "会员收货地址分页")
    @PostMapping("/receiveAddressPageList")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberReceiveAddressVo>> pageList(@RequestBody MemberReceiveAddressReqBo reqBo) {
        return Result.success(umsMemberReceiveAddressService.pageList(reqBo));
    }
}
