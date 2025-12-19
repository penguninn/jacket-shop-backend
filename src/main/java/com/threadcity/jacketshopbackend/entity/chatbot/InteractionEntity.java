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
@Table(name = "interactions")
public class InteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ⭐ Quan trọng: ID tự tăng
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "action_score", nullable = false)
    private Integer actionScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
