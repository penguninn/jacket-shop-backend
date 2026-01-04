package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews", schema = "dbo", indexes = {
        @Index(name = "IX_reviews_product", columnList = "product_id, created_at"),
        @Index(name = "IX_reviews_user", columnList = "user_id, created_at"),
        @Index(name = "IX_reviews_order", columnList = "order_id"),
        @Index(name = "IX_reviews_rating", columnList = "product_id, rating")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_reviews_user_product_order", columnNames = {"user_id", "product_id", "order_id"})
})
public class Review extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "user_name", nullable = false, length = 120)
    private String userName;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 2000)
    @Nationalized
    @Column(name = "comment", length = 2000)
    private String comment;

}
