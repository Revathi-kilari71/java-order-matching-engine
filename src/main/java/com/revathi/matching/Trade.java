package com.revathi.matching;

public record Trade(
        long sequence,
        long buyOrderId,
        long sellOrderId,
        long priceTicks,
        long quantity
) {
    public Trade {
        if (sequence <= 0 || buyOrderId <= 0 || sellOrderId <= 0) {
            throw new IllegalArgumentException(
                    "Sequence and order IDs must be positive"
            );
        }

        if (priceTicks <= 0 || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Trade price and quantity must be positive"
            );
        }
    }
}