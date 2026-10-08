package com.revathi.matching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplayRunnerTest {

    @Test
    void repeatedReplayProducesSameExpectedResults() {
        List<EngineCommand> commands = List.of(
                new EngineCommand.Submit(
                        new Order(1, Side.SELL, 10100, 5)),
                new EngineCommand.Submit(
                        new Order(2, Side.SELL, 10000, 4)),
                new EngineCommand.Submit(
                        new Order(3, Side.SELL, 10000, 6)),
                new EngineCommand.Submit(
                        new Order(4, Side.BUY, 10100, 7)),
                new EngineCommand.Cancel(3),
                new EngineCommand.Cancel(999)
        );

        var first = ReplayRunner.replay(commands);
        var second = ReplayRunner.replay(commands);

        assertEquals(first, second);

        assertEquals(List.of(
                new Trade(1, 4, 2, 10000, 4),
                new Trade(2, 4, 3, 10000, 3)
        ), first.trades());

        assertEquals(
                List.of(true, false),
                first.cancellationResults()
        );

        assertEquals(1, first.activeOrderCount());
                assertEquals(
                new BookSnapshot(
                        List.of(),
                        List.of(new BookSnapshot.Entry(1, 10100, 5))
                ),
                first.bookSnapshot()
        );
    }

    @Test
    void emptyReplayProducesEmptyResults() {
        var result = ReplayRunner.replay(List.of());

        assertTrue(result.trades().isEmpty());
        assertTrue(result.cancellationResults().isEmpty());
        assertEquals(0, result.activeOrderCount());
    }

    @Test
    void duplicateOrderIdStopsReplay() {
        List<EngineCommand> commands = List.of(
                new EngineCommand.Submit(
                        new Order(1, Side.BUY, 10000, 5)),
                new EngineCommand.Submit(
                        new Order(1, Side.SELL, 10000, 5))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ReplayRunner.replay(commands)
        );
    }
}