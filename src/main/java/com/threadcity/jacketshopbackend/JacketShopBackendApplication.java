package com.threadcity.jacketshopbackend;

import com.threadcity.jacketshopbackend.service.chatbot.DatabaseChatMemoryStore;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JacketShopBackendApplication {
    @AiService
    public interface Assistant {
        String chat(@MemoryId String userId, @UserMessage String message);
    }
    @Autowired
    private DatabaseChatMemoryStore memoryStore;
    public static void main(String[] args) {
        SpringApplication.run(JacketShopBackendApplication.class, args);
    }

}
