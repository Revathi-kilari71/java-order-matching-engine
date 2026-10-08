package com.revathi.matching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    @Test
    void matchesBestPriceThenArrivalOrder() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10100, 5));
        engine.submit(new Order(2, Side.SELL, 10000, 4));
        engine.submit(new Order(3, Side.SELL, 10000, 6));

        List<Trade> trades =
                engine.submit(new Order(4, Side.BUY, 10100, 7));

        assertEquals(List.of(
                new Trade(1, 4, 2, 10000, 4),
                new Trade(2, 4, 3, 10000, 3)
        ), trades);

        assertEquals(5, engine.remainingQuantity(1));
        assertEquals(3, engine.remainingQuantity(3));
        assertEquals(2, engine.activeOrderCount());
    }

    @Test
    void sellMatchesHighestBidAtRestingPrice() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.BUY, 9900, 5));
        engine.submit(new Order(2, Side.BUY, 10000, 3));

        List<Trade> trades =
                engine.submit(new Order(3, Side.SELL, 9800, 4));

        assertEquals(List.of(
                new Trade(1, 2, 3, 10000, 3),
                new Trade(2, 1, 3, 9900, 1)
        ), trades);

        assertEquals(4, engine.remainingQuantity(1));
        assertEquals(1, engine.activeOrderCount());
    }

    @Test
    void doesNotMatchWhenPricesDoNotCross() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10100, 5));

        List<Trade> trades =
                engine.submit(new Order(2, Side.BUY, 10000, 5));

        assertTrue(trades.isEmpty());
        assertEquals(2, engine.activeOrderCount());
    }

    @Test
    void incomingRemainderWaitsInBook() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10000, 3));
        engine.submit(new Order(2, Side.BUY, 10000, 8));

        assertEquals(0, engine.remainingQuantity(1));
        assertEquals(5, engine.remainingQuantity(2));
        assertEquals(1, engine.activeOrderCount());
    }

    @Test
    void cancelledOrderCannotTrade() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10000, 5));

        assertTrue(engine.cancel(1));
        assertFalse(engine.cancel(1));

        List<Trade> trades =
                engine.submit(new Order(2, Side.BUY, 10000, 5));

        assertTrue(trades.isEmpty());
        assertEquals(5, engine.remainingQuantity(2));
    }

    @Test
    void rejectsReusedIdAfterFullFill() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10000, 5));
        engine.submit(new Order(2, Side.BUY, 10000, 5));

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.submit(new Order(1, Side.BUY, 9000, 2))
        );

        assertEquals(0, engine.activeOrderCount());
    }
}