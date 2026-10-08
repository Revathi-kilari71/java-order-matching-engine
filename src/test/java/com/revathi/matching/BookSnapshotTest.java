package com.revathi.matching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookSnapshotTest {

    @Test
    void snapshotPreservesPricePriorityAndFifo() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.BUY, 9900, 2));
        engine.submit(new Order(2, Side.BUY, 10000, 3));
        engine.submit(new Order(3, Side.BUY, 10000, 4));

        engine.submit(new Order(4, Side.SELL, 10200, 5));
        engine.submit(new Order(5, Side.SELL, 10100, 6));
        engine.submit(new Order(6, Side.SELL, 10100, 7));

        assertEquals(
                new BookSnapshot(
                        List.of(
                                new BookSnapshot.Entry(2, 10000, 3),
                                new BookSnapshot.Entry(3, 10000, 4),
                                new BookSnapshot.Entry(1, 9900, 2)
                        ),
                        List.of(
                                new BookSnapshot.Entry(5, 10100, 6),
                                new BookSnapshot.Entry(6, 10100, 7),
                                new BookSnapshot.Entry(4, 10200, 5)
                        )
                ),
                engine.snapshot()
        );
    }

    @Test
    void snapshotShowsRemainingQuantityAfterPartialFill() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10000, 5));
        engine.submit(new Order(2, Side.BUY, 10000, 3));

        assertEquals(
                new BookSnapshot(
                        List.of(),
                        List.of(new BookSnapshot.Entry(1, 10000, 2))
                ),
                engine.snapshot()
        );
    }

    @Test
    void existingSnapshotDoesNotChangeWhenEngineChanges() {
        MatchingEngine engine = new MatchingEngine();

        engine.submit(new Order(1, Side.SELL, 10000, 5));
        BookSnapshot before = engine.snapshot();

        engine.submit(new Order(2, Side.BUY, 10000, 3));
        engine.cancel(1);

        assertEquals(
                List.of(new BookSnapshot.Entry(1, 10000, 5)),
                before.asks()
        );

        assertEquals(
                new BookSnapshot(List.of(), List.of()),
                engine.snapshot()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> before.asks().clear()
        );
    }
}