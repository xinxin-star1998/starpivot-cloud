package cn.org.starpivot.approval.service;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.approval.domain.dto.ApInstanceQueryDto;
import cn.org.starpivot.approval.domain.vo.ApInstanceVo;
import cn.org.starpivot.common.entity.PageResponse;

public interface ApInstanceService {

    Long submit(ApprovalSubmitRequest request, Long starterId);

    void withdraw(Long instanceId, Long starterId);

    void systemWithdraw(Long instanceId);

    PageResponse<ApInstanceVo> mineList(ApInstanceQueryDto query, Long starterId);

    ApprovalTimelineVo timeline(Long instanceId);
}
