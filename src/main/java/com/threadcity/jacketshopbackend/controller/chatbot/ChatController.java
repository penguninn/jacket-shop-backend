package com.threadcity.jacketshopbackend.controller.chatbot;

import com.threadcity.jacketshopbackend.service.chatbot.ChatbotService;
import com.threadcity.jacketshopbackend.service.chatbot.ChatHistoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(
            ChatbotService chatbotService,
            ChatHistoryService chatHistoryService
    ) {
        this.chatbotService = chatbotService;
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping
    public String chat(
            @RequestParam String userId,
            @RequestParam String message
    ) {
        chatHistoryService.saveUserMessage(userId, message);

        String answer = chatbotService.chat(message);

        chatHistoryService.saveAiMessage(userId, answer);

        return answer;
    }
}