package com.threadcity.jacketshopbackend.repository.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.InteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractionRepository extends JpaRepository<InteractionEntity, Long> {
    List<InteractionEntity> findByUserId(Long userId);
    List<InteractionEntity> findByProductId(Long productId);
    List<InteractionEntity> findAll();
}