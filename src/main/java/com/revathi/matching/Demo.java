package com.revathi.matching;

import java.util.List;

public final class Demo {

    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        // Higher-priced sell arrives first.
        engine.submit(new Order(1, Side.SELL, 10100, 5));

        // Two sells at the same lower price.
        engine.submit(new Order(2, Side.SELL, 10000, 4));
        engine.submit(new Order(3, Side.SELL, 10000, 6));

        System.out.println("BUY order 4: quantity 7, limit 10100");

        List<Trade> trades =
                engine.submit(new Order(4, Side.BUY, 10100, 7));

        for (Trade trade : trades) {
            System.out.printf(
                    "Trade %d: BUY=%d SELL=%d PRICE=%d QTY=%d%n",
                    trade.sequence(),
                    trade.buyOrderId(),
                    trade.sellOrderId(),
                    trade.priceTicks(),
                    trade.quantity()
            );
        }

        System.out.println(
                "Order 1 remaining: " + engine.remainingQuantity(1)
        );
        System.out.println(
                "Order 2 remaining: " + engine.remainingQuantity(2)
        );
        System.out.println(
                "Order 3 remaining: " + engine.remainingQuantity(3)
        );
        System.out.println(
                "Active orders before cancel: " + engine.activeOrderCount()
        );
        System.out.println("Cancel order 3: " + engine.cancel(3));
        System.out.println(
                "Active orders after cancel: " + engine.activeOrderCount()
        );
    }
}