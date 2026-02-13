package com.epam.xm.recommendations.infrastructure.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "crypto_prices", indexes = {
    @Index(name = "idx_crypto_prices_symbol_timestamp", columnList = "symbol, price_timestamp DESC")
})
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String symbol;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @NotNull
    @Column(name = "price_timestamp", nullable = false)
    private OffsetDateTime priceTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public PriceEntity() {
    }

    public PriceEntity(String symbol, BigDecimal price, OffsetDateTime priceTimestamp) {
        this.symbol = symbol;
        this.price = price;
        this.priceTimestamp = priceTimestamp;
    }

    public Long id() {
        return id;
    }

    public String symbol() {
        return symbol;
    }

    public BigDecimal price() {
        return price;
    }

    public OffsetDateTime priceTimestamp() {
        return priceTimestamp;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}
