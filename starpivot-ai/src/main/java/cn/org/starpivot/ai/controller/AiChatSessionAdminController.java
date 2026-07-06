package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiChatSessionQueryDto;
import cn.org.starpivot.ai.domain.vo.AiChatSessionAdminVo;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.ai.service.AiChatSessionAdminService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/session/admin")
@RequiredArgsConstructor
@Tag(name = "AI-会话管理", description = "管理员查看与管理 AI 对话会话")
public class AiChatSessionAdminController {

    private final AiChatSessionAdminService aiChatSessionAdminService;

    @Operation(summary = "会话分页列表")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:session:query')")
    public Result<PageResponse<AiChatSessionAdminVo>> pageList(@RequestBody AiChatSessionQueryDto query) {
        return Result.success(aiChatSessionAdminService.pageList(query));
    }

    @Operation(summary = "会话消息历史")
    @GetMapping("/messages")
    @PreAuthorize("hasAuthority('ai:session:query')")
    public Result<List<ChatHistoryMessageVo>> messages(@RequestParam String conversationId) {
        return Result.success(aiChatSessionAdminService.listMessages(conversationId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('ai:session:delete')")
    public Result<Void> remove(@PathVariable Long sessionId) {
        aiChatSessionAdminService.remove(sessionId);
        return Result.success();
    }
}
