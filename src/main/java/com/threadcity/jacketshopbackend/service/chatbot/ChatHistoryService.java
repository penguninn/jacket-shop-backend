package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.ChatMessageEntity;
import com.threadcity.jacketshopbackend.repository.chatbot.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatMessageRepository repository;

    public ChatHistoryService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public void saveUserMessage(String userId, String message) {
        repository.save(new ChatMessageEntity(userId, "user", message));
    }

    public void saveAiMessage(String userId, String message) {
        repository.save(new ChatMessageEntity(userId, "ai", message));
    }

    public List<ChatMessageEntity> getHistory(String userId) {
        return repository.findByUserIdOrderByTimestampAsc(userId);
    }
}

