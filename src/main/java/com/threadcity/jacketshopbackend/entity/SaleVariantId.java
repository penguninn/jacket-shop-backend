package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SaleVariantId implements Serializable {
    private static final long serialVersionUID = 840616966964555657L;
    @NotNull
    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @NotNull
    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        SaleVariantId entity = (SaleVariantId) o;
        return Objects.equals(this.saleId, entity.saleId) &&
                Objects.equals(this.productVariantId, entity.productVariantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId, productVariantId);
    }

}
