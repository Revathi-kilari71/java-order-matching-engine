package com.revathi.matching;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class ReplayDemo {

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: ReplayDemo [command-file]"
            );
        }

        Path path = Path.of(
                args.length == 1
                        ? args[0]
                        : "data/sample-commands.csv"
        );

        List<EngineCommand> commands = CommandFileReader.read(path);

        var first = ReplayRunner.replay(commands);
        var second = ReplayRunner.replay(commands);

        System.out.println("Commands loaded: " + commands.size());

        for (Trade trade : first.trades()) {
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
                "Cancellation results: " + first.cancellationResults()
        );

        System.out.println(
                "Active orders: " + first.activeOrderCount()
        );

        boolean sameResults = first.equals(second);

        System.out.println(
                "Replay results equal: " + sameResults
        );

        if (!sameResults) {
            throw new IllegalStateException("Replay results differ");
        }
    }
}