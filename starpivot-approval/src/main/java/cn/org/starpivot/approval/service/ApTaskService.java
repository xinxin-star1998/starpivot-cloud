package cn.org.starpivot.approval.service;

import cn.org.starpivot.approval.domain.dto.ApTaskActionDto;
import cn.org.starpivot.approval.domain.dto.ApTaskQueryDto;
import cn.org.starpivot.approval.domain.vo.ApTaskVo;
import cn.org.starpivot.common.entity.PageResponse;

public interface ApTaskService {

    PageResponse<ApTaskVo> todoList(ApTaskQueryDto query, Long assigneeId);

    PageResponse<ApTaskVo> doneList(ApTaskQueryDto query, Long assigneeId);

    void approve(ApTaskActionDto dto, Long operatorId);

    void reject(ApTaskActionDto dto, Long operatorId);
}
