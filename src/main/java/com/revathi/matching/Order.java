package com.revathi.matching;

import java.util.Objects;

public record Order(
        long id,
        Side side,
        long priceTicks,
        long quantity
) {
    public Order {
        if (id <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }

        Objects.requireNonNull(side, "Side is required");

        if (priceTicks <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}