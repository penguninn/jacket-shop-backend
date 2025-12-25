package com.threadcity.jacketshopbackend.entity.chatbot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String role; // user / ai / system

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String message;

    private LocalDateTime timestamp;

    public ChatMessageEntity(String userId, String role, String message) {
        this.userId = userId;
        this.role = role;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}