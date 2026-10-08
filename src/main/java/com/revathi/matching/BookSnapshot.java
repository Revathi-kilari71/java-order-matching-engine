package com.revathi.matching;

import java.util.List;

public record BookSnapshot(
        List<Entry> bids,
        List<Entry> asks
) {
    public BookSnapshot {
        bids = List.copyOf(bids);
        asks = List.copyOf(asks);
    }

    public record Entry(
            long orderId,
            long priceTicks,
            long remainingQuantity
    ) {
    }
}