package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.dto.SessionRenameDto;
import cn.org.starpivot.ai.domain.vo.AiHealthVo;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.ai.domain.vo.ChatReplyVo;
import cn.org.starpivot.ai.domain.vo.ChatSessionVo;
import cn.org.starpivot.ai.service.ChatService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI-对话", description = "通用 AI 智能对话")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<ChatReplyVo> send(@Valid @RequestBody ChatSendDto dto) {
        return Result.success(chatService.send(dto));
    }

    @Operation(summary = "流式发送消息（SSE）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatSendDto dto) {
        return chatService.stream(dto);
    }

    @Operation(summary = "服务健康与配置")
    @GetMapping("/health")
    public Result<AiHealthVo> health() {
        return Result.success(chatService.health());
    }

    @Operation(summary = "清空会话历史")
    @DeleteMapping("/history")
    public Result<Void> clearHistory(@RequestParam(required = false) String conversationId) {
        chatService.clearHistory(conversationId);
        return Result.success();
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/sessions")
    public Result<ChatSessionVo> createSession() {
        return Result.success(chatService.createSession());
    }

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public Result<List<ChatSessionVo>> listSessions() {
        return Result.success(chatService.listSessions());
    }

    @Operation(summary = "重命名会话")
    @PutMapping("/sessions/rename")
    public Result<ChatSessionVo> renameSession(@Valid @RequestBody SessionRenameDto dto) {
        return Result.success(chatService.renameSession(dto));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions")
    public Result<Void> deleteSession(@RequestParam String conversationId) {
        chatService.deleteSession(conversationId);
        return Result.success();
    }

    @Operation(summary = "会话消息历史")
    @GetMapping("/messages")
    public Result<List<ChatHistoryMessageVo>> listMessages(@RequestParam String conversationId) {
        return Result.success(chatService.listMessages(conversationId));
    }
}
