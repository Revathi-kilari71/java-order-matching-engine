package com.revathi.matching;

import java.util.*;

public final class MatchingEngine {

    // Highest buy price first.
    private final NavigableMap<Long, ArrayDeque<RestingOrder>> bids =
            new TreeMap<>(Comparator.reverseOrder());

    // Lowest sell price first.
    private final NavigableMap<Long, ArrayDeque<RestingOrder>> asks =
            new TreeMap<>();

    private final Map<Long, RestingOrder> activeOrders = new HashMap<>();
    private final Set<Long> usedOrderIds = new HashSet<>();

    private long tradeSequence = 0;

    public List<Trade> submit(Order order) {
        Objects.requireNonNull(order, "Order is required");

        if (!usedOrderIds.add(order.id())) {
            throw new IllegalArgumentException(
                    "Duplicate order ID: " + order.id()
            );
        }

        RestingOrder incoming = new RestingOrder(order);
        List<Trade> trades = new ArrayList<>();

        NavigableMap<Long, ArrayDeque<RestingOrder>> opposite =
                order.side() == Side.BUY ? asks : bids;

        while (!incoming.isFilled() && !opposite.isEmpty()) {
            long bestPrice = opposite.firstKey();

            boolean crosses = order.side() == Side.BUY
                    ? order.priceTicks() >= bestPrice
                    : order.priceTicks() <= bestPrice;

            if (!crosses) {
                break;
            }

            ArrayDeque<RestingOrder> queue = opposite.get(bestPrice);
            RestingOrder resting = queue.getFirst();

            long matchedQuantity = Math.min(
                    incoming.remainingQuantity(),
                    resting.remainingQuantity()
            );

            long buyId = order.side() == Side.BUY
                    ? order.id() : resting.order().id();

            long sellId = order.side() == Side.SELL
                    ? order.id() : resting.order().id();

            trades.add(new Trade(
                    ++tradeSequence,
                    buyId,
                    sellId,
                    bestPrice,
                    matchedQuantity
            ));

            incoming.fill(matchedQuantity);
            resting.fill(matchedQuantity);

            if (resting.isFilled()) {
                queue.removeFirst();
                activeOrders.remove(resting.order().id());
            }

            if (queue.isEmpty()) {
                opposite.remove(bestPrice);
            }
        }

        if (!incoming.isFilled()) {
            NavigableMap<Long, ArrayDeque<RestingOrder>> ownSide =
                    order.side() == Side.BUY ? bids : asks;

            ownSide.computeIfAbsent(
                    order.priceTicks(),
                    price -> new ArrayDeque<>()
            ).addLast(incoming);

            activeOrders.put(order.id(), incoming);
        }

        return List.copyOf(trades);
    }

    public boolean cancel(long orderId) {
        RestingOrder resting = activeOrders.get(orderId);

        if (resting == null) {
            return false;
        }

        Order order = resting.order();

        NavigableMap<Long, ArrayDeque<RestingOrder>> book =
                order.side() == Side.BUY ? bids : asks;

        ArrayDeque<RestingOrder> queue = book.get(order.priceTicks());
        queue.remove(resting);

        if (queue.isEmpty()) {
            book.remove(order.priceTicks());
        }

        activeOrders.remove(orderId);
        return true;
    }

    public long remainingQuantity(long orderId) {
        RestingOrder resting = activeOrders.get(orderId);
        return resting == null ? 0 : resting.remainingQuantity();
    }

    public int activeOrderCount() {
        return activeOrders.size();
    }
        public BookSnapshot snapshot() {
        return new BookSnapshot(
                snapshotEntries(bids),
                snapshotEntries(asks)
        );
    }

    private List<BookSnapshot.Entry> snapshotEntries(
            NavigableMap<Long, ArrayDeque<RestingOrder>> book
    ) {
        List<BookSnapshot.Entry> entries = new ArrayList<>();

        for (var level : book.entrySet()) {
            for (RestingOrder resting : level.getValue()) {
                entries.add(new BookSnapshot.Entry(
                        resting.order().id(),
                        level.getKey(),
                        resting.remainingQuantity()
                ));
            }
        }

        return List.copyOf(entries);
    }
}