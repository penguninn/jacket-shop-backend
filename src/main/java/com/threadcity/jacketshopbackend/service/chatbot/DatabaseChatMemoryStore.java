package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.ChatMessageEntity;
import com.threadcity.jacketshopbackend.repository.chatbot.ChatMessageRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseChatMemoryStore implements ChatMemoryStore {

    private final ChatMessageRepository repository;

    public DatabaseChatMemoryStore(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public void saveMessages(Object memoryId, List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            String role;
            if (message instanceof UserMessage) {
                role = "user";
            } else if (message instanceof AiMessage) {
                role = "ai";
            } else if (message instanceof SystemMessage) {
                role = "system";
            } else {
                role = "unknown";
            }

            repository.save(new ChatMessageEntity(memoryId.toString(), role, message.text()));
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        var entities = repository.findByUserIdOrderByTimestampAsc(memoryId.toString());
        List<ChatMessage> chatMessages = new ArrayList<>();

        for (var e : entities) {
            switch (e.getRole().toLowerCase()) {
                case "user" -> chatMessages.add(new UserMessage(e.getMessage()));
                case "ai", "assistant" -> chatMessages.add(new AiMessage(e.getMessage()));
                case "system" -> chatMessages.add(new SystemMessage(e.getMessage()));
                default -> chatMessages.add(new UserMessage(e.getMessage()));
            }
        }

        return chatMessages;
    }

    @Override
    public void deleteMessages(Object memoryId) {
        repository.deleteByUserId(memoryId.toString());
    }
    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        repository.deleteByUserId(memoryId.toString());
        saveMessages(memoryId, messages);
    }


}
