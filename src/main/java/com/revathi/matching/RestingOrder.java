package com.revathi.matching;

import java.util.Objects;

// Internal state of an order waiting in the order book.
final class RestingOrder {
    private final Order order;
    private long remainingQuantity;

    RestingOrder(Order order) {
        this.order = Objects.requireNonNull(order, "Order is required");
        this.remainingQuantity = order.quantity();
    }

    Order order() {
        return order;
    }

    long remainingQuantity() {
        return remainingQuantity;
    }

    void fill(long quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException("Invalid fill quantity");
        }

        remainingQuantity -= quantity;
    }

    boolean isFilled() {
        return remainingQuantity == 0;
    }
}