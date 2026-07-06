package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.dto.AiUsageLogQueryDto;
import cn.org.starpivot.ai.domain.entity.AiUsageLog;
import cn.org.starpivot.ai.domain.vo.AiUsageLogVo;
import cn.org.starpivot.ai.domain.vo.AiUsageSummaryVo;
import cn.org.starpivot.ai.mapper.AiUsageLogMapper;
import cn.org.starpivot.ai.service.AiUsageLogService;
import cn.org.starpivot.common.entity.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiUsageLogServiceImpl implements AiUsageLogService {

    private final AiUsageLogMapper aiUsageLogMapper;
    private final AiUsageLogAsyncWriter aiUsageLogAsyncWriter;

    @Override
    public void recordSuccess(ChatResponse response, UsageContext context) {
        AiUsageLog entity = baseLog(context, "0");
        Usage usage = response != null && response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        if (usage != null) {
            entity.setPromptTokens(safeInt(usage.getPromptTokens()));
            entity.setCompletionTokens(safeInt(usage.getCompletionTokens()));
            entity.setTotalTokens(safeInt(usage.getTotalTokens()));
        } else {
            entity.setPromptTokens(estimateTokens(context.userMessageLength()));
            entity.setCompletionTokens(estimateTokens(context.completionLength()));
            entity.setTotalTokens(entity.getPromptTokens() + entity.getCompletionTokens());
        }
        aiUsageLogAsyncWriter.write(entity);
    }

    @Override
    public void recordFailure(UsageContext context, String errorMessage) {
        AiUsageLog entity = baseLog(context, "1");
        entity.setErrorMessage(trimError(errorMessage));
        entity.setPromptTokens(estimateTokens(context.userMessageLength()));
        aiUsageLogAsyncWriter.write(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AiUsageSummaryVo summary(String beginTime, String endTime) {
        AiUsageSummaryVo summary = aiUsageLogMapper.selectSummary(beginTime, endTime);
        return summary != null ? summary : new AiUsageSummaryVo();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiUsageLogVo> pageList(AiUsageLogQueryDto query) {
        Page<AiUsageLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiUsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getUserId() != null, AiUsageLog::getUserId, query.getUserId())
                .eq(StringUtils.hasText(query.getModel()), AiUsageLog::getModel, query.getModel())
                .eq(StringUtils.hasText(query.getRequestType()), AiUsageLog::getRequestType, query.getRequestType())
                .ge(StringUtils.hasText(query.getBeginTime()), AiUsageLog::getCreateTime, query.getBeginTime())
                .le(StringUtils.hasText(query.getEndTime()), AiUsageLog::getCreateTime, query.getEndTime())
                .orderByDesc(AiUsageLog::getCreateTime);
        Page<AiUsageLog> result = aiUsageLogMapper.selectPage(page, wrapper);
        PageResponse<AiUsageLogVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    private AiUsageLog baseLog(UsageContext context, String success) {
        AiUsageLog entity = new AiUsageLog();
        entity.setUserId(context.userId());
        entity.setConversationId(context.conversationId());
        entity.setModel(context.model());
        entity.setRequestType(context.requestType());
        entity.setLatencyMs(context.latencyMs());
        entity.setSuccess(success);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    private AiUsageLogVo toVo(AiUsageLog entity) {
        AiUsageLogVo vo = new AiUsageLogVo();
        vo.setLogId(entity.getLogId());
        vo.setUserId(entity.getUserId());
        vo.setConversationId(entity.getConversationId());
        vo.setModel(entity.getModel());
        vo.setRequestType(entity.getRequestType());
        vo.setPromptTokens(entity.getPromptTokens());
        vo.setCompletionTokens(entity.getCompletionTokens());
        vo.setTotalTokens(entity.getTotalTokens());
        vo.setLatencyMs(entity.getLatencyMs());
        vo.setSuccess(entity.getSuccess());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private int estimateTokens(int length) {
        if (length <= 0) {
            return 0;
        }
        return Math.max(1, length / 2);
    }

    private String trimError(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
