package com.threadcity.jacketshopbackend.repository.chatbot;

import com.threadcity.jacketshopbackend.entity.chatbot.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByUserIdOrderByTimestampAsc(String userId);
    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}
