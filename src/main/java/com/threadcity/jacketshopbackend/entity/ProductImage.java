package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images", schema = "dbo", indexes = {
        @Index(name = "IX_product_images_product", columnList = "product_id")
})
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ColumnDefault("sysdatetimeoffset()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    @Size(max = 500)
    @NotNull
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_thumbnail", nullable = false)
    @Builder.Default
    private Boolean isThumbnail = false;

}
