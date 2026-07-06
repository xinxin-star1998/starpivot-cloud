package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.dto.AiUsageLogQueryDto;
import cn.org.starpivot.ai.domain.vo.AiUsageLogVo;
import cn.org.starpivot.ai.domain.vo.AiUsageSummaryVo;
import cn.org.starpivot.common.entity.PageResponse;
import org.springframework.ai.chat.model.ChatResponse;

public interface AiUsageLogService {

    void recordSuccess(ChatResponse response, UsageContext context);

    void recordFailure(UsageContext context, String errorMessage);

    AiUsageSummaryVo summary(String beginTime, String endTime);

    PageResponse<AiUsageLogVo> pageList(AiUsageLogQueryDto query);

    record UsageContext(
            Long userId,
            String conversationId,
            String model,
            String requestType,
            long latencyMs,
            int userMessageLength,
            int completionLength) {}
}
