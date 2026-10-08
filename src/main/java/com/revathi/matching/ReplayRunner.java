package com.revathi.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReplayRunner {

    private ReplayRunner() {
    }

    public record ReplayResult(
            List<Trade> trades,
            List<Boolean> cancellationResults,
            int activeOrderCount,
            BookSnapshot bookSnapshot
    ) {
        public ReplayResult {
            trades = List.copyOf(trades);
            cancellationResults = List.copyOf(cancellationResults);
            Objects.requireNonNull(bookSnapshot, "Snapshot is required");
        }
    }

    public static ReplayResult replay(List<EngineCommand> commands) {
        Objects.requireNonNull(commands, "Commands are required");

        List<EngineCommand> input = List.copyOf(commands);
        MatchingEngine engine = new MatchingEngine();

        List<Trade> trades = new ArrayList<>();
        List<Boolean> cancellationResults = new ArrayList<>();

        for (EngineCommand command : input) {
            switch (command) {
                case EngineCommand.Submit submit ->
                        trades.addAll(engine.submit(submit.order()));

                case EngineCommand.Cancel cancel ->
                        cancellationResults.add(
                                engine.cancel(cancel.orderId())
                        );
            }
        }

        return new ReplayResult(
                trades,
                cancellationResults,
                engine.activeOrderCount(),
                engine.snapshot()
        );
    }
}