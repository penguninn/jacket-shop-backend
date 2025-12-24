package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
    boolean existsBySku(String sku);

    List<ProductVariant> findAllByProductId(Long productId);

    boolean existsByProductIdAndColorIdAndSizeIdAndMaterialId(Long productId, Long colorId, Long sizeId, Long materialId);

    Optional<ProductVariant> findBySku(String sku);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.quantity = pv.quantity + :quantityChange, pv.availableQuantity = pv.availableQuantity + :quantityChange WHERE pv.id = :id")
    void adjustStock(@Param("id") Long id, @Param("quantityChange") int quantityChange);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.reservedQuantity = pv.reservedQuantity + :quantity, pv.availableQuantity = pv.availableQuantity - :quantity WHERE pv.id = :id AND pv.availableQuantity >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.reservedQuantity = pv.reservedQuantity - :quantity, pv.availableQuantity = pv.availableQuantity + :quantity WHERE pv.id = :id")
    void releaseReservedStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.reservedQuantity = pv.reservedQuantity - :quantity, pv.quantity = pv.quantity - :quantity, pv.soldCount = pv.soldCount + :quantity WHERE pv.id = :id")
    void commitReservedStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.quantity = pv.quantity - :quantity, pv.availableQuantity = pv.availableQuantity - :quantity, pv.soldCount = pv.soldCount + :quantity WHERE pv.id = :id AND pv.availableQuantity >= :quantity")
    int directDeductStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.quantity = pv.quantity + :quantity, pv.availableQuantity = pv.availableQuantity + :quantity, pv.soldCount = pv.soldCount - :quantity WHERE pv.id = :id")
    void returnStock(@Param("id") Long id, @Param("quantity") int quantity);
}
