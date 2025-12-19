package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.InteractionEntity;
import com.threadcity.jacketshopbackend.repository.chatbot.InteractionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteractionService {

    private final InteractionRepository repo;

    public InteractionService(InteractionRepository repo){
        this.repo = repo;
    }

    public List<InteractionEntity> getInteractionsOfUser(Long userId){
        return repo.findByUserId(userId);
    }
}
