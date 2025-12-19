package com.threadcity.jacketshopbackend.controller.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.ChatMessageEntity;
import com.threadcity.jacketshopbackend.service.chatbot.ChatHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/history")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping
    public List<ChatMessageEntity> history(@RequestParam String userId) {
        return chatHistoryService.getHistory(userId);
    }
}
